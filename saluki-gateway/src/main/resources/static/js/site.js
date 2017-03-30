$(function () {
    $('[data-toggle="tooltip"]').tooltip();
    $('*[data-remove-confirm]').on('click', function(event){
        if(!confirm($(this).attr('data-remove-confirm'))) {
            event.preventDefault();
        }
    });
})