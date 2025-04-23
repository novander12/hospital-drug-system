<template>
  <router-view />
</template>

<script setup>
// No script needed here usually, App.vue becomes just a router outlet
</script>

<style>
/* Global styles can remain or be moved to a separate CSS file imported in main.js */
#app {
  font-family: 'Microsoft YaHei', sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  height: 100vh; /* Ensure app takes full height */
  margin: 0; /* Remove default body margin */
}

/* Reset body margin/padding */
body {
  margin: 0;
  padding: 0;
}
</style>