$(function () {
    $("#publishBtn").click(publish);
});

function publish() {
    $("#publishModal").modal("hide");
    //发送ajax请求之前，将CSRF令牌设置到请求的消息头中
    // var token=$("meta[name='_csrf']").attr("content");
    // var header=$("meta[name='_csrf_header']").attr("content");
    // $(document).ajaxSend(function (e,xhr,options) {
    //     xhr.setRequestHeader(header,token);
    // })

    //获取标题和内容
    var title = $("#recipient-name").val();
    var content = $("#message-text").val();

    $.post(
        "/community/discuss/add",
        {"title": title,"content": content},
        function (data) {
            //服务端返回的json字符串转为json对象
            data = $.parseJSON(data);
            //在提示框中显示msg
            $("#hintBody").text(data.msg)
            //显示提示框
            $("#hintModal").modal("show");
            //2秒后，自动隐藏提示框
            setTimeout(function () {
                $("#hintModal").modal("hide");
                //刷新页面
                if (data.code == 0) {
                    window.location.reload();
                }
            }, 2000);
        }
    );

}