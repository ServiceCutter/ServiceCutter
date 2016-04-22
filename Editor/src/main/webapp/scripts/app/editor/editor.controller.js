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
        };
        
        $scope.deleteSystem = function () {
        	if($scope.modelId != 0) {
        		Model.delete({id:$scope.modelId}, function () {
        			$scope.modelId = 0;
        			$scope.loadAvailableModels();
        		});
        	}
        };
        
        $scope.loadAvailableModels = function () {
        	$scope.availableModels = Model.all(function () {
        		$scope.status = 'Select a model or upload a new one.';
        	});
        }
        
        // this is an adjacent 3-colors set created by http://paletton.com/
        // the starting color is #337ab7 which is the primary color of bootstrap
        $scope.colors = ['#337BB7', '#4442C2', '#2BBE83', '#7EB1DC', '#8B89E1', // color 1 
                         '#79DFB6', '#5393C8', '#6361D0', '#4CCD99', '#1265AB', // color 2 
                         '#2522B7', '#08B36E', '#094B83', '#16148D', '#008A52']; // color 3
        $scope.contextColors = {};
        $scope.$watch('model.nanoentities', function() {
        	var index = 0;
        	if($scope.model != null) {
        		angular.forEach($scope.model.nanoentities, function(nanoentity) {
        			var context = nanoentity.context;
        			if(!(context in $scope.contextColors)) {
        				$scope.contextColors[context] = $scope.colors[index++ % $scope.colors.length];
        			}
        		});
        	}
        });
        
        $scope.getColor = function (context) {
        	return $scope.contextColors[context];
        }
        
        $scope.userRepStatus = '';
        $scope.status = '';
        $scope.modelId = ($rootScope.modelId == undefined ? 0 : $rootScope.modelId) ;
        $scope.model = null;
        $scope.modelsById = {};
        $scope.loadAvailableModels();
        $scope.ccFilter = '';
    });
