let app = angular.module('mainApp', []);
app.controller('mainController', function ($scope) {

    $scope.messageItems = [];
    $scope.connectionButtonText = "Connect"

    let autoScrollFlag = true;
    let source;

    $scope.connectionToSSE = function () {
        if (source !== undefined) {
            source.close();
            source = undefined;
            $scope.connectionButtonText = "Connect";
            return;
        }
        if ('EventSource' in window) {
            source = new EventSource($("#connectionUrl").val(), {withCredentials: true});
            // console.log(source.readyState);
            source.onopen = function (event) {
                let messageItem = {};
                messageItem.date = Date();
                messageItem.title = "Successfully connected to the server!";
                $scope.messageItems.push(messageItem);
                updateMessageContent();
            };
            source.onmessage = function (event) {
                let data = event.data;
                let origin = event.origin;
                let messageItem = {};
                messageItem.date = Date();
                messageItem.title = "Message From Server(origin:" + origin + "): ";
                messageItem.content = data;
                $scope.messageItems.push(messageItem);
                updateMessageContent();
            };
            source.onerror = function (event) {
                let messageItem = {};
                messageItem.date = Date();
                messageItem.title = "There is an error with connection";
                $scope.messageItems.push(messageItem);
                source.close();
                source = undefined;
                $scope.connectionButtonText = "Connect";
                updateMessageContent();

            };
        }
        $scope.connectionButtonText = "Disconnect";
    }
    $scope.clearMessageContent = function () {
        $scope.messageItems = [];
    }
    $scope.messageTextareaMouseOver = function () {
        autoScrollFlag = false;
    };
    $scope.messageTextareaMouseOut = function () {
        autoScrollFlag = true;
    };

    function updateMessageContent() {
        while ($scope.messageItems.length > 100) {
            $scope.messageItems.shift();
        }
        $scope.$digest();
        if (autoScrollFlag) {
            document.getElementById("messageTextarea").scrollTop = document.getElementById("messageTextarea").scrollHeight;
        }
    }

});
