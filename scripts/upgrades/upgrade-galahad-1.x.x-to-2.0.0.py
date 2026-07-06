#!/usr/bin/env python3

# Upgrade Galahad from 1.x.x to 2.0.0.
#
# In 2.0.0, lemma and pos were moved from Term.lemma and Term.pos to Term.annotation["lemma"] and Term.annotation["pos"]
# in order to allow for more annotations to be added to a Term.
# Additionally, the public field was removed from the corpus metadata and a language field was added.
#
# This upgrade script parses all json in the ARG1 folder and updates the json to the new format.

import json
from argparse import ArgumentParser
from pathlib import Path


def upgrade_corpora(input: Path, output: Path) -> None:
    # list all corpora (folders) in the input folder
    corpora = list(input.iterdir())
    total = len(corpora)
    print(f"Upgrading [{total}] corpora")
    for i, corpus in enumerate(corpora):
        print(f"[{i + 1}/{total}] Upgrading corpus {corpus}")
        corpus_out = output / corpus.name
        corpus_out.mkdir(parents=True, exist_ok=True)
        update_corpus(corpus, corpus_out)
        print()


def update_corpus(corpus: Path, output: Path) -> None:
    update_metadata(corpus, output)
    update_documents(corpus, output)


def update_metadata(corpus: Path, output: Path) -> None:
    # remove the public field from the metadata.json
    # and add a default language field
    print("  Upgrading metadata")
    metadata_path = corpus / "metadata"
    if metadata_path.exists():
        # open file
        metadata = json.loads(metadata_path.read_text(encoding="utf-8"))
        # remove the public field
        if "public" in metadata:
            metadata.pop("public")
            print("    Removed public field from metadata.json")
        # add language field
        if "language" not in metadata:
            metadata["language"] = "Dutch"
            print("    Added language field to metadata.json")
        # remove eraTo and eraFrom fields if they exist, replace with period object with to and from fields
        if "eraTo" in metadata or "eraFrom" in metadata:
            era_to = metadata.pop("eraTo", None)
            era_from = metadata.pop("eraFrom", None)
            metadata["period"] = {"to": era_to, "from": era_from}
            print("    Replaced eraTo and eraFrom fields with period object in metadata.json")
        # remove sourceName and sourceURL fields if they exist, replace with source object with name and url fields
        if "sourceName" in metadata or "sourceURL" in metadata:
            source_name = metadata.pop("sourceName", None)
            source_url = metadata.pop("sourceURL", None)
            metadata["source"] = {"name": source_name, "url": source_url}
            print("    Replaced sourceName and sourceURL fields with source object in metadata.json")
        # write to .json
        metadata_out = output / "metadata.json"  # 2.0 uses file extension for metadata
        metadata_out.write_text(json.dumps(metadata), encoding="utf-8")
    else:
        print("    metadata not found")


def update_documents(corpus: Path, output: Path) -> None:
    # update all documents in the corpus
    print("  Upgrading documents")
    docs_folder = corpus / "documents"
    documents = list(docs_folder.iterdir())
    total = len(documents)
    for i, document in enumerate(documents):
        print(f"    [{i + 1}/{total}] Upgrading document {document}")
        doc = docs_folder / document.name / "uploaded" / document.name
        # copy over to output
        doc_out = output / "layers/source/documents" / document.stem / "file" / document.name
        doc_out.parent.mkdir(parents=True, exist_ok=True)
        doc_out.write_text(doc.read_text(encoding="utf-8"), encoding="utf-8")


if __name__ == "__main__":
    parser = ArgumentParser(description="Upgrade Galahad from 1.x.x to 2.0.0")
    parser.add_argument("input", type=Path, help="Path to the folder containing the corpora to upgrade")
    parser.add_argument("output", type=Path, help="Path to the folder where the upgraded corpora will be saved")
    args = parser.parse_args()
    args.output.mkdir(parents=True, exist_ok=True)
    upgrade_corpora(args.input, args.output)
