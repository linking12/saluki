$(document).ready(function(){
	var TTEXT_MIME_TYPE = 'application/x-thrift; protocol=TTEXT';
    var specification = {};
    var navTemplate = Handlebars.compile($('#nav-template').html());
    var previousFragmentPath = null;
    var options = {
              mode: 'form',
              modes: ['code', 'form', 'text', 'tree', 'view'],
    };
    var container_request = document.getElementById("editor_holder_request");
	var editor_request = new JSONEditor(container_request, options);
	var container_response = document.getElementById("editor_holder_response");
	var editor_response = new JSONEditor(container_response, options);
    $.getJSON('service/getAllService', function (data) {
	      specification = {services:data}
	      $('.sidebar').html(navTemplate({specification: specification}));
	});
	$('#function').hide();
	$('#functionbutton').hide();
    $(window).trigger('hashchange');
    $(window).on('hashchange', function () {
        render(URI(window.location.href));
    });
    $('#editor_btn_submitParam').click(function() {
             editor_response.set('{There is have no response}');
             $('#editor_btn_submitParam').attr('disabled',"true");
	         var data = {
	           service:$('#servicetest_serviceName').val(),
	           method:$('#servicetest_methodName').val(),
	           parameterType:$('#servicetest_requestType').val(),
	           returnType:$('#servicetest_responseType').val(),
	           parameter:JSON.stringify(editor_request.get()) 
	         };
	         $.ajax({ 
	            type: "POST", 
	            url: "service/testLocal?routerRule="+URLencode($('#routerRule').val()), 
	            contentType: "application/json",  
	            data: JSON.stringify(data),
	            success: function(result) { 
	               $('#editor_btn_submitParam').removeAttr("disabled");
	               editor_response.set(result);
	            },
	            error : function(jqXHR, textStatus, errorThrown){
	                $('#editor_holder_response').width(600);
	                $('#editor_holder_response').height(200);
	                $('#editor_holder_response').html("<p class='text-info'>"+jqXHR.responseText+"</p>");
	                $('#editor_btn_submitParam').removeAttr("disabled");
	                setInterval(function(){
	                   self.local="/doc";
	                }, 3000);  
	            }
	         }); 
    });
    function URLencode(sStr){
       return escape(sStr).replace(/\+/g, '%2B').replace(/\"/g,'%22').replace(/\'/g, '%27').replace(/\//g,'%2F');
    }
    function render(uri) {
	    var fragmentUri = uri.fragment(true);
	    var path = fragmentUri.pathname();
	    if (path == previousFragmentPath) {
	      return;
	    }
	    previousFragmentPath = path;
	    var hashSplit = path.split('/');
	    var prefix = hashSplit[0];
	    var mapping = {
	      'function': function (serviceName, functionName) {
	        renderFunction(serviceName, functionName);
	      }
	    };
	    if (mapping[prefix]) {
	       mapping[prefix].apply(this, hashSplit.slice(1));
	    } 
    }
    function renderFunction(serviceName, functionName){
         $('#home').hide();
         $('#function').show();
         $('#functionbutton').show();
         $('#page_header').html('<code title="'+serviceName+'.'+functionName+'()">'+serviceName+'.'+functionName+'()</code>');
         $('#servicetest_serviceName').val(serviceName)
         $('#servicetest_methodName').val(functionName),
         $.getJSON("service/getMethod",{ service: serviceName, method: functionName },function(result){
			 editor_request.set(result.parameterTypes[0]);
			 $('#servicetest_requestType').val(result.parameterTypes[0].class);
			 $('#servicetest_responseType').val(result.returnType.class);
		 });
    }
    
}); 

