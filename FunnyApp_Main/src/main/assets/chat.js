
function connectWebViewJavascriptBridge(callback) {
    if (window.WebViewJavascriptBridge) {
        callback(WebViewJavascriptBridge)
    } else {
        document.addEventListener(
            'WebViewJavascriptBridgeReady'
            , function() {
                callback(WebViewJavascriptBridge)
            },
            false
        );
    }
}

function call_sys(strFunction,strData,myFunction){
    window.WebViewJavascriptBridge.callHandler(
        strFunction
        , strData
        , function(responseData) {
            if (myFunction==null || myFunction === undefined){
                $("#show").html("data = " + responseData);
            }else{
                myFunction(responseData);
            }
        }
    );

}

function list_friend(){

    call_sys('send_msg_sys',
        {type:"list.all",from: "[user]", to:"", message: ""},
        function(){
            $("#friend_list").html("<option value='*' selected>*</option>");
        });
}


function test1_call(){

    call_sys('test1',"",function(data){
        $("#show").html(data);
    });
}

function chat_history(){

    call_sys('chat_ids',"",function(data){
        $("#show").html("");
        chat_read(data);
    });
}

function chat_read(ids){
    if (ids=="") return ;

    var strSplit=ids.split(",");
    for (var i=0;i<strSplit.length;i++){
        call_sys('chat_read',strSplit[i],function(data){

            var parser = new DOMParser();
            var xmlDoc = parser.parseFromString(
                "<data>"+data+"</data>","text/xml");
            var id=xmlDoc.getElementsByTagName("id")[0].innerHTML;
            var time=xmlDoc.getElementsByTagName("time")[0].innerHTML;
            var msg=xmlDoc.getElementsByTagName("msg")[0].innerHTML;
            var from=xmlDoc.getElementsByTagName("from")[0].innerHTML;
            var to=xmlDoc.getElementsByTagName("to")[0].innerHTML;
            var strLine;

            if (from==user_name){
                strLine=id+"=我:"+time+"<br>"
                                            +msg+"<br><br>";
            }else{
                strLine=id+"="+from+":"+time+"<br><font color=blue>"
                            +msg+"</font><br><br>";
            }
            $("#show").append(strLine);
        });
    }
}

function send_msg() {
    var str1 = $("#tx_msg").val();
    var to=$("#friend_list").val();

    call_sys('send_msg',
        {"to":to,"message":str1},
        function(){
            chat_history();
        });

}
