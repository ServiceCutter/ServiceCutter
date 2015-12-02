'use strict';

angular.module('editorApp')
    .controller('EditorController', function ($scope, $http, Principal, Upload, VisDataSet, Model) {
        Principal.identity().then(function(account) {
            $scope.account = account;
            $scope.isAuthenticated = Principal.isAuthenticated; 
        });
        
        $scope.$watch('file', function () {
        	$scope.upload($scope.file, 'model', 'status', true);
        });
        
        $scope.$watch('transactionsFile', function () {
        	$scope.upload($scope.transactionsFile, 'model/'+ $scope.modelId+'/transactions', 'transactionStatus', false);
        });

        $scope.$watch('separationCriteriaFile', function () {
        	$scope.upload($scope.separationCriteriaFile, 'model/'+ $scope.modelId+'/separationCriteria', 'separationStatus', false);
        });
        
        $scope.$watch('cohesiveGroupsFile', function () {
        	$scope.upload($scope.cohesiveGroupsFile, 'model/'+ $scope.modelId+'/cohesiveGroups', 'cohesiveGroupsStatus', false);
        });
        
        $scope.$watch('characteristicsFile', function () {
        	$scope.upload($scope.characteristicsFile, 'model/'+ $scope.modelId+'/distanceVariants', 'characteristicsStatus', false);
        });
        
        $scope.$watch('modelId', function () {
        	$scope.showModel();
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
        
        $scope.listFieldNames = function (fields) {
        	return fields.map(function (o,i) {
        		return o.name;
        	}).join(', ');
        }
        
        
        $scope.transactionStatus = 'No upload yet.';
        $scope.separationStatus = 'No upload yet.';
        $scope.cohesiveGroupsStatus = 'No upload yet.';
        $scope.characteristicsStatus = 'No upload yet.';
        $scope.status = 'No upload yet.';
        $scope.modelId = 0;
        $scope.model = null;
        $scope.modelsById = {};
        $scope.availableModels = Model.all(function(models) {
        	$scope.status = 'Select a model or upload a new one.';
        });
    });
