from enum import Enum
import re
import string
import xml.etree.ElementTree as ET
import os
import subprocess
import logging
import sys

logging.basicConfig(
    level=logging.WARNING, stream=sys.stdout, format="[%(levelname)s] %(message)s"
)
WINDOW_SIZE = 10
PUNCTUATION = ".,;:!?‘’'[]()/\"-—–“”«»„…*"


class Direction(Enum):
    """
    Useful for calculating array offset in both direction.
    """

    FORWARD = 1
    BACKWARD = -1


class Analyser:
    """
    Analyse the output of galahad tagged (predicted) documents against the ground truth.
    Performs:
    1. Simple analysis:
        counts the number of <w> and <pc> tags in each file.
    2. Advanced analysis:
        matches <w> and <pc> tags 1-on-1 by literal. Traverses the xml tree,
        offsetting the index when a mismatch is found.
    """

    truth_folder: str
    galahad_folder: str
    is_truth_plaintext: bool
    # Number of matching documents.
    n_simple_doc_matches: int
    n_advanced_doc_matches: int
    # Used for advanced analysis.
    offset: int

    def __init__(
        self,
        truth_folder: str = "truth",
        galahad_folder: str = "galahad",
        is_truth_plaintext: bool = False,
    ):
        self.is_truth_plaintext = is_truth_plaintext
        self.truth_folder = truth_folder
        self.galahad_folder = galahad_folder

    def analyse(self):
        self.n_simple_doc_matches = 0
        self.n_advanced_doc_matches = 0
        _, _, files = next(os.walk(self.galahad_folder))
        for file in files:
            print(f"{file} -- {self.get_document_id(self.galahad_folder, file)}")
            self.simple_analysis(file)
            self.advanced_analysis(file)
            print("\n")

        self.print_summary(files)

    def get_document_id(self, folder: str, file: str) -> str:
        try:
            root = ET.parse(f"{folder}/{file}").getroot()
            for tag in root.iter():
                # tag.tag contains a namespace, hence "in".
                if "idno" in tag.tag and tag.attrib["type"] == "pid":
                    document_id = f"{tag.text}"
                    return document_id
        except:
            pass
        return "No ID"

    def simple_analysis(self, file: str) -> None:
        n_tags_in_pred = self.count_tags_simple(self.galahad_folder, file)
        n_tags_in_truth = self.count_tags_simple(self.truth_folder, file)
        if n_tags_in_pred == n_tags_in_truth:
            self.n_simple_doc_matches += 1
        logging.info("Simple analysis:")
        logging.info(f"Tags in galahad: {n_tags_in_pred}")
        logging.info(f"Tags in ground truth: {n_tags_in_truth}")

    def count_tags_simple(self, folder: str, file: str) -> int:
        return int(
            subprocess.check_output(
                f'cat "{folder}/{file}" | grep -oE "</w>|</pc>" | wc -l',
                shell=True,
                text=True,
            ).strip()
        )

    def advanced_analysis(self, f: str) -> None:
        """
        Match <w> and <pc> tags 1-on-1 between the two files.
        """
        logging.info("Advanced analysis:")
        pred_tags, truth_tags = self.get_tag_lists(f)
        # print number of tags
        logging.debug(f"Tags in pred: {len(pred_tags)}")
        logging.debug(f"Tags in truth: {len(truth_tags)}")
        matches = self.advanced_tag_matching(pred_tags, truth_tags)
        self.print_advanced_summary(truth_tags, matches)

    def get_tag_lists(self, file: str) -> tuple[list[str], list[str]]:
        pred_tags = self.extract_tags_from_file(self.galahad_folder, file)
        truth_tags = self.extract_tags_from_file(
            self.truth_folder, file, self.is_truth_plaintext
        )
        return pred_tags, truth_tags

    def extract_tags_from_file(
        self, folder: str, file: str, is_plaintext: bool = False
    ) -> list[str]:
        """
        Returns list of text content of all <w> and <pc> tags from the file.
        """
        tags: list[str] = []
        root = ET.parse(f"{folder}/{file}").getroot()
        for element in root.iter():
            tag = element.tag.split("}")[-1]  # tag has a weird namespace.
            if is_plaintext:
                if tag == "body":
                    text_content = "".join(list(element.itertext()))
                    return self.tokenize(text_content)
            else:
                if tag == "w" or tag == "pc":
                    # Get text by iterating over all textable children.
                    text_content = "".join(list(element.itertext()))
                    tags.append(text_content)
        return tags

    def tokenize(self, text: str) -> list[str]:
        """
        Tokenizes text by splitting on whitespace-like.
        """
        tokens = re.split(rf"[{string.whitespace}]", text)
        return [token for token in tokens if token != ""]

    def advanced_tag_matching(self, pred_tags: list[str], truth_tags: list[str]) -> int:
        """
        Matches tags one-on-one between the two files.
        """
        matches: int = 0
        self.offset: int = 0
        for i in range(len(truth_tags)):
            if i + self.offset >= len(pred_tags):
                logging.debug(f"Reached end of pred tags. Stopping.")
                break
            truth_text = truth_tags[i]
            pred_text = pred_tags[i + self.offset]
            if self.text_matches(truth_text, pred_text):
                matches += 1
            else:
                logging.debug(
                    f"Tags did not match.\n\tTruth: {truth_text}\n\tPred: {pred_text}"
                )
                matches = self.resolve_mismatch(i, pred_tags, truth_text, matches)
        return matches

    def text_matches(self, truth_text: str, pred_text: str) -> bool:
        """
        Checks if the two texts match, ignoring punctuation.
        """
        return truth_text.strip(PUNCTUATION) == pred_text.strip(PUNCTUATION)

    def resolve_mismatch(
        self, i: int, pred_tags: list[str], truth_text: str, matches: int
    ) -> int:
        # if truth_text consists completely of punctuation, we ignore it.
        if truth_text.strip(PUNCTUATION) == "":
            logging.debug(f"Ignoring punctuation.")
            return matches + 1  # Just consider it a match.
        # Try to find the correct shift to apply to offset.
        forward_shift: int | None = self.find_shifted_match(
            pred_tags, i, truth_text, Direction.FORWARD
        )
        backward_shift: int | None = self.find_shifted_match(
            pred_tags, i, truth_text, Direction.BACKWARD
        )

        # If we found a match, we shift the offset.
        if forward_shift is not None or backward_shift is not None:
            # Get smallest non-null abs(shift).
            shift = min(filter(None, [forward_shift, backward_shift]), key=abs)
            logging.debug(f"Found match. Shifted offset forward by {shift}.")
            self.offset += shift
            return matches + 1
        else:
            pred_text = pred_tags[i + self.offset]
            logging.error(
                f"Could not resolve mismatch:\n\tTruth: {truth_text}\n\tPred: {pred_text}"
            )
            return matches

    def find_shifted_match(
        self, pred_tags: list[str], i: int, truth_text: str, dir: Direction
    ) -> int | None:
        """
        Tries to find a match for truth_text WINDOW_SIZE, forward or backward.
        Returns the required shift to apply to the offset.
        """
        for undirectional_j in range(1, WINDOW_SIZE):
            # Make j go forward or backward.
            j = undirectional_j * dir.value
            # Out of bounds check.
            if i + self.offset + j >= len(pred_tags) or i + self.offset + j < 0:
                return None
            # Match checking.
            pred_text: str = pred_tags[i + self.offset + j]
            if self.text_matches(truth_text, pred_text):
                logging.debug(f"Found potential match. Shift: {j}")
                return j
        return None

    def print_advanced_summary(self, truth_tags: list[str], matches: int) -> None:
        logging.info(f"Advanced matches: {matches}")
        if matches == len(truth_tags):
            print(f"[SUCCESS] Found all tags with advanced matching.")
            self.n_advanced_doc_matches += 1
        else:
            print(f"[FAIL] Advanced matching misses {len(truth_tags) - matches} tags.")

    def print_summary(self, files: list[str]) -> None:
        print(f"Summary:")
        print(f"\tSimple matches: {self.n_simple_doc_matches}")
        print(f"\tAdvanced matches: {self.n_advanced_doc_matches}")
        print(f"\tTotal files: {len(files)}")


if __name__ == "__main__":
    Analyser("CLVN-truth", "CLVN-hug").analyse()
