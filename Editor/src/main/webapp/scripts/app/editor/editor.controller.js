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
			$scope.inputError = undefined;
            $scope.jsonError = undefined;
            $scope.jsonErrorModel = undefined;
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
					if(resp['data']['id']){
						$scope.availableModels = Model.all();
						$scope.modelId = parseInt(resp['data']['id']);
					}
					$scope.updateFields(resp, 'status', 'jsonErrorModel');
				}); 

            }
        };
        
        $scope.uploadUserReps = function (file) {
            if (file && !file.$error) {
            	$scope['userRepStatus'] = 'Uploading...';
				Upload.upload({
					url: 'api/editor/model/'+ $scope.modelId+'/userrepresentations',
					file: file,
					progress: function(e){}
				}).then(function (resp) {
					$scope.loadCoupling($scope.modelId);
					$scope.updateFields(resp, 'userRepStatus', 'jsonError');
		        });
            }
        };
        
        $scope.updateFields = function(resp, statusfield, jsonErrorDiv){
			$scope[statusfield] = resp['data']['message'];
			$scope.inputError = resp['data']['warnings'];
			$scope[jsonErrorDiv] = resp['data']['jsonError'];
        }

        
        
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
