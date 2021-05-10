let app = angular.module('mainApp', []);
app.controller('mainController', function ($scope) {

    $scope.messageContent = "";
    let messageItems = [];

    let autoScrollFlag = true;

    $scope.connectionToSSE = function () {
        if ('EventSource' in window) {
            let source = new EventSource($("#connectionUrl").val(), {withCredentials: true});
            // console.log(source.readyState);
            source.onopen = function (event) {
            };
            source.onmessage = function (event) {
                let data = event.data;
                let origin = event.origin;
                let lastEventId = event.lastEventId;
                let messageItem = {};
                messageItem.date = Date();
                messageItem.title = "Message(lastEventId:" + lastEventId + ") From Server(origin:" + origin + "): ";
                messageItem.content = data;
                messageItems.push(messageItem);
                while (messageItems.length > 100) {
                    messageItems.shift();
                }
                $scope.messageContent = messageItems.join("");
                $scope.$digest();
                if (autoScrollFlag) {
                    document.getElementById("messageTextarea").scrollTop = document.getElementById("messageTextarea").scrollHeight;
                }
            };
            source.onerror = function (event) {
            };
        }
    }
    $scope.clearMessageContent=function (){
        $scope.messageContent = "";
    }
    $scope.messageTextareaMouseOver = function () {
        autoScrollFlag = false;
    };
    $scope.messageTextareaMouseOut = function () {
        autoScrollFlag = true;
    };

});
