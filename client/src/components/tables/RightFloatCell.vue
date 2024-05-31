<!-- A GTable cell that contains a (flex-growing) main component on the left and optional sticky right floating component -->
<template>
    <div class="flex">
        <div class="left">
            <slot name="left"></slot>
        </div>
        <div>
            <slot name="right"></slot>
        </div>
    </div>
</template>
<style scoped>
.flex {
    display: flex;
    align-items: center;
    gap: 1em;
}
.left {
    text-align: center;
    flex: 1;
}
</style>