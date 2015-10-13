'use strict';

angular.module('editorApp')
    .controller('EditorController', ['$scope', '$http', 'Principal', 'Upload', 'VisDataSet', function ($scope, $http, Principal, Upload, VisDataSet) {
        Principal.identity().then(function(account) {
            $scope.account = account;
            $scope.isAuthenticated = Principal.isAuthenticated; 
        });
        
        $scope.graphOptions = {
			autoResize: true,
			height: '100%',
			width: '100%',
			nodes: { shadow: { enabled: true, size: 5 } },
			edges: { shadow: { enabled: true, size: 5 } }
		};
        $scope.$watch('file', function () {
        	$scope.upload($scope.file, 'model', 'status', true);
        });
        
        $scope.$watch('transactionsFile', function () {
        	$scope.upload($scope.transactionsFile, 'model/'+ $scope.modelId+'/transactions', 'transactionStatus', false);
        });
        
        $scope.$watch('modelId', function () {
        	$scope.showModel();
        });
        
        $scope.$watch("model['entities']", function () {
        	if ($scope.model != null && $scope.model.entities != null) {
    			var contextNodes = new VisDataSet([]);
    			var contextEdges = new VisDataSet([]);
    			var nodeId = 1;
    			var entitiesById = {};
    			$scope.modelsById = {};
    			for (var x in $scope.model.entities) {
    				var name = $scope.model.entities[x].name;
    				contextNodes.add({id: nodeId, label: name});
    				entitiesById[name] = nodeId;
    				$scope.modelsById[nodeId] = name;
    				nodeId++;
    			}
    			// TODO add real data!
    			var relations = $scope.model['relations'];
    			//alert(JSON.stringify(aggregations));
    			for (var x in relations) {
    				var relation = relations[x];
    				if(relation.criterionType == 'COMPOSITION_ENTITY') {
    					var names = relation.name.split('.');
    					var from = names[0];
    					var to = names[1];
    					var edge = {from: entitiesById[from], to: entitiesById[to], arrows: 'middle', width: 1};
    					contextEdges.add(edge);
    				}
    				if(relation.criterionType == 'AGGREGATED_ENTITY') {
    					var names = relation.name.split('.');
    					var from = names[0];
    					var to = names[1];
    					var edge = {from: entitiesById[from], to: entitiesById[to], arrows: 'middle', dashes: true, width: 1};
    					contextEdges.add(edge);
    				}
    				if(relation.criterionType == 'INHERITANCE') {
    					var names = relation.name.split('.');
    					var from = names[0];
    					var to = names[1];
    					var edge = {from: entitiesById[from], to: entitiesById[to], arrows: 'to', width: 1};
    					contextEdges.add(edge);
    				}
    			}
    	        $scope.graphData = {
	            	'nodes': contextNodes,
	            	'edges': contextEdges
	            };
        	}
        });

        $scope.selectNode = function(param) {
        	if(param.nodes.length > 0) { // TODO handle multi-select
        		var firstNode = parseInt(param.nodes[0]);
        		var selectedNode = $scope.modelsById[firstNode];
        		$scope.$apply(function() {
        			$scope.model.filteredDataFields = $scope.model.dataFields.filter(function(field) {return selectedNode == field.context;});
        		});
        	} else {
        		$scope.$apply(function() {
        			$scope.model.filteredDataFields = $scope.model.dataFields;
        		});
        	}
        };
        
        $scope.graphResize = function(param) {
        	this.fit(); // Zooms out so all nodes fit on the canvas.
        };

        $scope.graphEvents = {
        	selectNode: $scope.selectNode,
        	deselectNode: $scope.selectNode,
        	resize: $scope.graphResize
        };
        
        $scope.upload = function (file, url, statusField, reload) {
            if (file && !file.$error) {
            	$scope[statusField] = 'Uploading...';
				Upload.upload({
					url: 'api/editor/'+url,
					file: file,
					progress: function(e){}
				}).success(function(data, status, headers, config) {
					$scope[statusField] = 'Upload successful!';
					if(reload){
						$scope.loadAvailableModels();
						$scope.modelId = parseInt(data['id']);
					}
				}).error(function (data, status, headers, config) {
					$scope[statusField] = 'Upload failed! (' + data['error'] + ')';
		        }); 

            }
        };
        
        $scope.showModel = function () {
        	if($scope.modelId != 0) {
        		$http.get('/api/engine/models/' + $scope.modelId).
	        		success(function(data) {
	        			$scope.model = data;
	        			$scope.model.filteredDataFields = $scope.model.dataFields;
	            });
        		$http.get('/api/engine/models/' + $scope.modelId + '/couplingcriteria').
	        		success(function(data) {
	        			$scope.model['relations'] = data;
	        			$scope.model['entities'] = data.filter(function(item) { return item.criterionType == 'SAME_ENTITIY' ;});
                });        		
        	}
        };
        
        $scope.loadAvailableModels = function () {
    		$http.get('api/engine/models').
	    		success(function(data) {
	    			$scope.availableModels = data;
            });
        };
        
        $scope.status = 'No upload yet.';
        $scope.modelId = 0;
        $scope.model = null;
        $scope.isFullScreen = false;
        $scope.modelsById = {};
        $scope.loadAvailableModels();
    }]);
