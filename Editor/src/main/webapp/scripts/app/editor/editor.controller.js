'use strict';

angular.module('editorApp')
    .controller('EditorController', function ($scope, $rootScope,$http, Principal, Upload, VisDataSet, Model) {
        Principal.identity().then(function(account) {
            $scope.account = account;
            $scope.isAuthenticated = Principal.isAuthenticated; 
        });
        
        $scope.$watch('file', function () {
        	$scope.upload($scope.file, 'model', 'status', true);
        });
        
        $scope.$watch('userRepFile', function () {
        	$scope.upload($scope.userRepFile, 'model/'+ $scope.modelId+'/userrepresentations', 'userRepStatus', false);
        });

        $scope.$watch('modelId', function () {
        	$scope.showModel();
        	$rootScope.modelId = $scope.modelId;
        });
        
        $scope.upload = function (file, url, statusField, fullReload) {
            if (file && !file.$error) {
            	$scope[statusField] = 'Uploading...';
				Upload.upload({
					url: 'api/editor/'+url,
					file: file,
					progress: function(e){}
				}).success(function(data, status, headers, config) {
					$scope[statusField] = 'Upload successful!';
					if(fullReload){
						$scope.availableModels = Model.all();
						$scope.modelId = parseInt(data['id']);
					} else {
						$scope.loadCoupling($scope.modelId)
					}
				}).error(function (data, status, headers, config) {
					$scope[statusField] = 'Upload failed! (' + data['error'] + ')';
		        }); 

            }
        };
        
        $scope.showModel = function () {
        	if($scope.modelId != 0) {
        		$scope.model = Model.get({id:$scope.modelId}, function(model) {
        			$scope.loadCoupling($scope.modelId);
        		});
        	}
        };
        
        $scope.loadCoupling = function (id) {
        	Model.getCoupling({id:id}, function(coupling) {
        		$scope.model['coupling'] = coupling;
        	});
        }
        
        $scope.listNanoentityNames = function (nanoentities) {
        	return nanoentities.map(function (o,i) {
        		return o.name;
        	}).join(', ');
        }
        
        
        $scope.userRepStatus = 'No upload yet.';
        $scope.status = 'No upload yet.';
        $scope.modelId = ($rootScope.modelId == undefined ? 0 : $rootScope.modelId) ;
        $scope.model = null;
        $scope.modelsById = {};
        $scope.availableModels = Model.all(function(models) {
        	$scope.status = 'Select a model or upload a new one.';
        });
    });
