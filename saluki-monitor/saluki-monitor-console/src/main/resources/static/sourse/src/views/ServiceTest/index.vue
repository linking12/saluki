<template>
  <div id="service-test">
    <div class="row">
      <div class="col-md-12 col-lg-12 col-sm-12">
        <div class="panel panel-default">
          <div class="panel-heading">服务测试</div>
          <div class="panel-body">
            <p>service ：{{ $route.params.service }}</p>
            <p>method ：{{ methodName }}</p>
          </div>
        </div>
      </div>
    </div>
    <div class="row">
      <div class="col-md-9 col-lg-9 col-sm-8">
        <div class="white-box">
          <div>
            <p class="text-blue">请求参数（json）</p>
            <div class="editor-json" ref="editorJsonEdit"></div>
            <br>
            <p class="text-center">
              <button class="btn btn-lg btn-primary" @click="testMethod()"> 开始测试 </button>
            </p>
          </div>
        </div>
        <div class="white-box">
          <div>
            <p class="text-blue">响应（json）</p>
            <div class="editor-json" ref="editorJsonShow"></div>
          </div>
        </div>
      </div>
      <div class="col-md-3 col-lg-3 col-sm-4">
        <div class="white-box">
          其他方法
          <hr>
          <p v-for="item in methodList">
            <a class="btn btn-sm btn-default" @click="getMethodInfo(item.name)">
              {{ item.name }}
            </a>
          </p>
        </div>
      </div>
    </div>
  </div>
</template>

<script type="text/babel">
  import 'jsoneditor/dist/jsoneditor.min.css';
  import JSONEditor from 'jsoneditor/dist/jsoneditor.min.js';

  export default {
    data(){
      return{
        methodName: null,
        methodList: [],
        methodInfo: null,
        editorEdit: null,
        editorShow: null
      }
    },
    mounted(){
      // create the editor
      const optionForEdit = {
        mode: 'form',
        modes: ['code', 'form', 'text', 'tree', 'view'], // allowed modes
        onError: function (err) {
          alert(err.toString());
        },
        onModeChange: function (newMode, oldMode) {
          console.log('Mode switched from', oldMode, 'to', newMode);
        }
      };
      const optionForShow = {
        mode: 'form',
        modes: ['code', 'form', 'text', 'tree', 'view'], // allowed modes
        onError: function (err) {
          alert(err.toString());
        },
        onModeChange: function (newMode, oldMode) {
          console.log('Mode switched from', oldMode, 'to', newMode);
        }
      };
      this.editorEdit = new JSONEditor(this.$refs.editorJsonEdit, optionForEdit);
      this.editorShow = new JSONEditor(this.$refs.editorJsonShow, optionForShow);

      // list
      this.getAllMethod();
    },
    methods:{
      getAllMethod(){
        const url = `/api/serviceMeasure/getAllMethod?`+
                    `ipPort=${this.$route.params.ipPort}`+
                    `&service=${this.$route.params.service}`;
        this.$http.get(url).then((response)=>{
          this.methodList = response.body;
          if(this.methodName == null){
            this.methodName = response.body[0].name;
            this.getMethodInfo(response.body[0].name);
          }
        },(response)=>{
          this.methodList = [];
        });
      },
      getMethodInfo(method){
        if(!method && !this.methodName){
          this.methodInfo = null;
          return;
        }
        if(method){
          this.methodName = method;
        }
        const url = `/api/serviceMeasure/getMethod?`+
            `ipPort=${this.$route.params.ipPort}`+
            `&service=${this.$route.params.service}`+
            `&method=${this.methodName}`;
        this.$http.get(url).then((response)=>{
          this.methodInfo = response.body;
          this.editorEdit.set(response.body.parameterTypes[0]);
          this.editorShow.set(response.body.returnType);
        },(response)=>{
          this.methodInfo = null;
        })
      },
      testMethod(){
        const url = `/api/serviceMeasure/testService?ipPort=${this.$route.params.ipPort}`;
        const params = {
          method: this.methodName,
          parameter: JSON.stringify(this.editorEdit.get()),
          parameterType: this.methodInfo.parameterTypes[0].class,
          returnType: this.methodInfo.returnType.class,
          service: this.$route.params.service
        };
        this.$http.post(url, params).then((response)=>{
          this.editorShow.set(response.body)
        },(response)=>{
          this.editorShow.set(response.body)
        });
      }
    }
  }
</script>

<style lang="stylus">
  edit-layout-color = #EDF1F5
  .editor-json
    width 100%
    height 350px
    .jsoneditor
      border-color edit-layout-color
      .jsoneditor-menu
        border-color edit-layout-color
        background-color edit-layout-color
        button
          background-color #999
        .jsoneditor-frame
          button
            background-color inherit
</style>
