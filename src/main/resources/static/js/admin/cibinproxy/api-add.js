$(function () {
    $('.skin-minimal input').iCheck({
        checkboxClass: 'icheckbox-blue',
        radioClass: 'iradio-blue',
        increaseArea: '20%'
    });

    // private String apiName;
    // private String method = "GET";
    // private String paramNames;
    // private String checkedParams;
    // private Boolean securityCheck;
    // private Boolean active = true;
    // private String dependency;
    // private String requestPath;

    $("#form-admin-add").validate({
        rules:{
            apiName:{
                required:true,
            },
            paramNames:{
                required:true
            },
            checkedParams:{
                required:true,
            },
            securityCheck:{
                required:true
            },
            requestPath:{
                required:true
            }
        },
        onkeyup:false,
        focusCleanup:true,
        success:"valid",
        submitHandler:function(form){
            $(form).ajaxSubmit({
                type: 'post',
                url: "/admin/cibinproxy/api/save",
                dataType:"json",
                success: function(data){
                    if(data.status == "success"){
                        succeedMessage(data.message);
                        var index = parent.layer.getFrameIndex(window.name);
                        parent.location.reload();
                        parent.layer.close(index);
                    }else {
                        errorMessage(data.message);
                    }
                }
            });
            return false; // 非常重要，如果是false，则表明是不跳转，在本页上处理，也就是ajax，如果是非false，则传统的form跳转。
        }
    });
});