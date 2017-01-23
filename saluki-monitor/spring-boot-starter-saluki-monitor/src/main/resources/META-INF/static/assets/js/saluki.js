$(document).ready(function(){
	var TTEXT_MIME_TYPE = 'application/x-thrift; protocol=TTEXT';
    var specification = {};
    var navTemplate = Handlebars.compile($('#nav-template').html());
    var previousFragmentPath = null;
    $.getJSON('service/getAllService', function (data) {
	      specification = {services:data}
	      $('.sidebar').html(navTemplate({specification: specification}));
	      
	});
    $(window).trigger('hashchange');
    $(window).on('hashchange', function () {
        render(URI(window.location.href));
    });
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
        var serviceInfo = specification.services[serviceName];
	    if (serviceInfo == undefined) {
	      return;
	    }
	    var functionInfo = serviceInfo.functions[functionName];
	    if (functionInfo == undefined) {
	      return;
	    }
	    processService(serviceInfo);
	    processFunction(functionInfo);
	    makeActive('li#nav-' + serviceName + '.' + functionName);
	    var oldDebugHttpHeadersText = functionContainer.find('.debug-http-headers');
	    var oldDebugHttpHeadersSticky = functionContainer.find('.debug-http-headers-sticky');
	    functionContainer.html(functionTemplate({
	      'serviceName': serviceName,
	      'serviceSimpleName': serviceInfo.simpleName,
	      'serviceEndpoints': serviceInfo.endpoints,
	      'serviceDebugPath': serviceInfo.debugPath,
	      'serviceDebugFragment': serviceInfo.debugFragment,
	      'function': functionInfo
	    }));
        
    }
    
    
    
   
    
}); 

