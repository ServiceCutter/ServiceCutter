'use strict';

angular.module('editorApp')
    .controller('EditorController', ['$scope', 'Principal', 'Upload', function ($scope, Principal, Upload) {
        Principal.identity().then(function(account) {
            $scope.account = account;
            $scope.isAuthenticated = Principal.isAuthenticated;
        });
        
        $scope.status = 'No upload yet.';
        
        $scope.$watch('file', function () {
        	$scope.upload($scope.file);
        });

        $scope.upload = function (file) {
            if (file && !file.$error) {
            	$scope.status = 'uploading';
				Upload.upload({
					url: 'editor/upload',
					file: file,
					progress: function(e){}
				}).then(function(data, status, headers, config) {
					$scope.status = data['data'];
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
        
    }]);
