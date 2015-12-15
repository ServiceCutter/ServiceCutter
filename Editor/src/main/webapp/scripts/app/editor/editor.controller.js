'use strict';

angular.module('editorApp')
    .controller('EditorController', function ($scope, $rootScope,$http, Principal, Upload, VisDataSet, Model) {
        Principal.identity().then(function(account) {
            $scope.account = account;
            $scope.isAuthenticated = Principal.isAuthenticated; 
        });
        

        $scope.$watch('modelId', function () {
        	$scope.showModel();
        	$rootScope.modelId = $scope.modelId;
            $scope.userRepStatus = '';
            $scope.status = '';
            $scope.jsonError = undefined;
        });
        
        $scope.$watch('file', function () {
        	$scope.uploadModel($scope.file);
        });
        
        $scope.$watch('userRepFile', function () {
        	$scope.uploadUserReps($scope.userRepFile);
        });

        
        $scope.uploadModel = function (file) {
            if (file && !file.$error) {
            	$scope['status'] = 'Uploading...';
				Upload.upload({
					url: 'api/editor/model',
					file: file,
					progress: function(e){}
				}).then(function (resp) {
					$scope['status'] = resp['data']['message'];
					$scope.availableModels = Model.all();
					$scope.modelId = parseInt(resp['data']['id']);
				}, function (resp) {
					$scope['status'] = resp['data']['message'];
					$scope.jsonError = resp['data']['jsonError'];
		        }); 

            }
        };
        
        $scope.uploadUserReps = function (file, url, statusField, modelUpload) {
            if (file && !file.$error) {
            	$scope['userRepStatus'] = 'Uploading...';
				Upload.upload({
					url: 'api/editor/model/'+ $scope.modelId+'/userrepresentations',
					file: file,
					progress: function(e){}
				}).then(function (resp) {
					$scope['userRepStatus'] = resp['data']['message'];
					$scope.jsonError = resp['data']['jsonError'];
					$scope.loadCoupling($scope.modelId)
		        }, function (resp) {
					$scope['userRepStatus'] = resp['data']['message'];
					$scope.jsonError = resp['data']['jsonError'];

		        }, function (evt) {});
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
        
        
        $scope.userRepStatus = '';
        $scope.status = '';
        $scope.modelId = ($rootScope.modelId == undefined ? 0 : $rootScope.modelId) ;
        $scope.model = null;
        $scope.modelsById = {};
        $scope.availableModels = Model.all(function(models) {
        	$scope.status = 'Select a model or upload a new one.';
        });
    });
