<template>
  <div id="page-wrapper" class="animated fadeInDown">
    <div class="container-fluid">
      <!-- <div class="row bg-title">
      </div> -->
      <!---->
      <div class="row">
        <div class="col-md-12 col-lg-12 col-sm-12"></div>
      </div>
      <!---->
      <slot name="content"></slot>
    </div>
  </div>
</template>

<style lang="stylus">
  #page-wrapper
    .container-fluid
      height 100%
</style>
