'use strict';

angular.module('editorApp')
    .controller('EditorController', ['$scope', '$http', 'Principal', 'Upload', function ($scope, $http, Principal, Upload) {
        Principal.identity().then(function(account) {
            $scope.account = account;
            $scope.isAuthenticated = Principal.isAuthenticated;
        });
        
        $scope.status = 'No upload yet.';
        $scope.modelId = 0;
        $scope.model = null;
        
        $scope.$watch('file', function () {
        	$scope.upload($scope.file);
        });
        
        $scope.upload = function (file) {
            if (file && !file.$error) {
            	$scope.status = 'uploading';
				Upload.upload({
					url: 'api/editor/upload',
					file: file,
					progress: function(e){}
				}).then(function(data, status, headers, config) {
					$scope.status = data['data']['message'];
					$scope.modelId = parseInt(data['data']['id']);
					$scope.showModel();
				}); 

//                for (var i = 0; i < files.length; i++) {
//                  var file = files[i];
//                  if (!file.$error) {
//                    Upload.upload({
//                        url: 'https://angular-file-upload-cors-srv.appspot.com/upload',
//                        fields: {
//                            'username': $scope.username
//                        },
//                        file: file
//                    }).progress(function (evt) {
//                        var progressPercentage = parseInt(100.0 * evt.loaded / evt.total);
//                        $scope.log = 'progress: ' + progressPercentage + '% ' +
//                                    evt.config.file.name + '\n' + $scope.log;
//                    }).success(function (data, status, headers, config) {
//                        $timeout(function() {
//                            $scope.log = 'file: ' + config.file.name + ', Response: ' + JSON.stringify(data) + '\n' + $scope.log;
//                        });
//                    });
//                  }
//                }
            }
        };
        
        $scope.showModel = function () {
        	if($scope.modelId != 0) {
        		$http.get('http://localhost:8090/engine/models/' + $scope.modelId).
	        		success(function(data) {
	        			$scope.model = data;
	                });
        		
        	}
        }
        
    }]);
