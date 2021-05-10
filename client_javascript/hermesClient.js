let app = angular.module('mainApp', []);
app.controller('mainController', function ($scope) {

    $scope.messageItems = [];

    let autoScrollFlag = true;

    $scope.connectionToSSE = function () {
        if ('EventSource' in window) {
            let source = new EventSource($("#connectionUrl").val(), {withCredentials: true});
            // console.log(source.readyState);
            source.onopen = function (event) {
                let messageItem = {};
                messageItem.date = Date();
                messageItem.title = "Successfully connected to the server!";
                $scope.messageItems.push(messageItem);
                while ($scope.messageItems.length > 100) {
                    $scope.messageItems.shift();
                }
                $scope.$digest();
            };
            source.onmessage = function (event) {
                let data = event.data;
                let origin = event.origin;
                let messageItem = {};
                messageItem.date = Date();
                messageItem.title = "Message From Server(origin:" + origin + "): ";
                messageItem.content = data;
                $scope.messageItems.push(messageItem);
                while ($scope.messageItems.length > 100) {
                    $scope.messageItems.shift();
                }
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
        $scope.messageItems = [];
    }
    $scope.messageTextareaMouseOver = function () {
        autoScrollFlag = false;
    };
    $scope.messageTextareaMouseOut = function () {
        autoScrollFlag = true;
    };

});
