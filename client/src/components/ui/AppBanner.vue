<template>
    <hgroup class="banner" role="banner">
        <div class="logo">
            <router-link to="/">
                <img class="painting" src="@/assets/logo/galahad-graal-klein.png" />
            </router-link>
        </div>

        <div class="title">
            <a href="https://ivdnt.org/" target="_blank">/ instituut voor de Nederlandse&nbsp;taal&nbsp;/</a>
            <router-link to="/">galahad</router-link>
        </div>

        <nav class="nav">
            <a href="/galahad/help" target="_blank">help</a>
            <a href="/galahad/application/about" target="_blank">about</a>
            <a href="/galahad/application/contribute" target="_blank">contribute</a>
            <a href="https://portal.clarin.ivdnt.org/lancelot" target="_blank">lancelot</a>
        </nav>
    </hgroup>
</template>

<style scoped lang="scss">
// Header top
.banner {
    background-color: white;
    align-items: center;
    display: flex;
    flex-wrap: wrap;
    height: 70px;

    // header top tabs start
    .logo {
        align-self: flex-start;

        a {
            display: block;
            height: 70px;
            width: 152px;
            position: relative;

            img {
                position: relative;
                left: -3px;
                top: -1px;
                height: 82px;
                pointer-events: none;
            }
        }
    }

    // header top title
    .title {
        display: inline-flex;
        flex-direction: column;
        padding: 0px 20px;

        a {
            font-style: normal;
            color: black;
            text-decoration: none;
            font-size: 0.8rem;

            &:last-child {
                font-size: 1.7rem;
                line-height: 1.7rem;
            }
        }
    }

    // header top tabs end
    .nav {
        flex: 1;
        display: flex;
        justify-content: flex-end;
        flex-wrap: wrap;

        a {
            margin-right: 30px;
            font-size: 16px;
            color: black;
            text-decoration: none;
            font-style: normal;

            &:hover {
                text-decoration: underline;
                cursor: pointer;
            }
        }
    }
}

@media (max-width: 730px) {
    .logo {
        display: none;
    }
}
</style>
