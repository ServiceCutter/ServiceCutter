'use strict';

angular.module('editorApp')
    .controller('EditorController', ['$scope', '$http', 'Principal', 'Upload', 'VisDataSet', function ($scope, $http, Principal, Upload, VisDataSet) {
        Principal.identity().then(function(account) {
            $scope.account = account;
            $scope.isAuthenticated = Principal.isAuthenticated;
        });
        
        $scope.graphOptions = {
			autoResize: true,
			height: '400',
			width: '100%'
		};
        
        $scope.$watch('file', function () {
        	$scope.upload($scope.file);
        });
        
        $scope.$watch('modelId', function () {
        	$scope.showModel();
        });
        
        $scope.$watch("model['entities']", function () {
        	if ($scope.model != null && $scope.model.entities != null) {
    			var contextNodes = new VisDataSet([]);
    			var contextEdges = new VisDataSet([]);
    			var nodeId = 1;
    			for (var x in $scope.model.entities) {
    				contextNodes.add({id: nodeId, label: $scope.model.entities[x].name});
    				nodeId++;
    			}
    			// TODO add real data!
    			contextEdges.add({from: 1, to: 2, arrows: 'to'});
    			contextEdges.add({from: 3, to: 4, arrows: 'to'});
    	        $scope.graphData = {
	            	'nodes': contextNodes,
	            	'edges': contextEdges
	            };
        	}
        });
        
        $scope.upload = function (file) {
            if (file && !file.$error) {
            	$scope.status = 'Uploading...';
				Upload.upload({
					url: 'api/editor/upload',
					file: file,
					progress: function(e){}
				}).success(function(data, status, headers, config) {
					$scope.status = data['message'];
					$scope.loadAvailableModels();
					$scope.modelId = parseInt(data['id']);
				}).error(function (data, status, headers, config) {
					$scope.status = 'Upload failed! (' + data['error'] + ')';
		        }); 

            }
        };
        
        $scope.showModel = function () {
        	if($scope.modelId != 0) {
        		$http.get($scope.config['engineUrl'] + '/engine/models/' + $scope.modelId).
	        		success(function(data) {
	        			$scope.model = data;
	            });
        		$http.get($scope.config['engineUrl'] + '/engine/models/' + $scope.modelId + '/couplingcriteria?type=SAME_ENTITIY').
	        		success(function(data) {
	        			$scope.model['entities'] = data;
                });        		
        	}
        };

        $scope.loadConfig = function () {
    		$http.get('/api/editor/config').
	    		success(function(data) {
	    			$scope.config = data;
	    			$scope.loadAvailableModels();
            });
        };
        
        $scope.loadAvailableModels = function () {
    		$http.get($scope.config['engineUrl'] + '/engine/models').
	    		success(function(data) {
	    			$scope.availableModels = data;
            });
        }
        
        $scope.status = 'No upload yet.';
        $scope.modelId = 0;
        $scope.model = null;
        $scope.loadConfig();
    }]);
