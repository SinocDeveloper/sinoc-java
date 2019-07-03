/**
 * Render home page.
 *  - show blockchain tree chart;
 *  - show blockchain info
 */

(function() {
    'use strict';

    function showToastr(topMessage, bottomMessage) {
    	if(!topMessage){
    		topMessage = bottomMessage;
    	}
    	if(!bottomMessage){
    		bottomMessage = "";
    	}
        toastr.clear()
        toastr.options = {
            "positionClass": "toast-top-right",
            "closeButton": true,
            "timeOut": "4000"
        };
        toastr.warning('<strong>' + topMessage + '</strong> <br/><small>' + bottomMessage + '</small>');
    }

    function LoginCtrl($scope, $http,$location,$rootScope) {
    	
    	document.getElementById("login").style.height = $(window).height() - 60+"px";
    	
        $scope.user = {
            userName:'',
            password:''
        }

        $scope.login = function(){
            let message = "";
            if(!$scope.user.userName){
            	var topMessage = "user name can't be empty！";
            }
            if(!$scope.user.password){
            	var bottomMessage = "password can't be empty！";
            }
            if(message != ""){
            	showToastr(topMessage,bottomMessage);
            	return ;
            }
            
            
            $http({
                method: 'get',
                url: '/user/login',
                params:{
                	userName:$scope.user.userName,
                	password:$scope.user.password
                }
            }).then(function(result) {
            	
            	if(result.data.success){
            		if(result.data && result.data.userName){
                		$rootScope.clientInfo = result.data;
                		$location.path("/");
                	}
            	}else{
            		showToastr(result.data.errMsg);
            	}
            	
            },function(e){
            	showToastr("server is busy,please try again later!");
            	
            });
        }
    }

    angular.module('HarmonyApp').controller('LoginCtrl', ['$scope','$http','$location','$rootScope', LoginCtrl]);
})();
