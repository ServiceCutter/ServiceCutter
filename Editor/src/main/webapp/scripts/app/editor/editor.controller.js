'use strict';

angular.module('editorApp')
    .controller('EditorController', function ($scope, $http, Principal, Upload, VisDataSet, Model) {
        Principal.identity().then(function(account) {
            $scope.account = account;
            $scope.isAuthenticated = Principal.isAuthenticated; 
        });
        
        $scope.graphOptions = {
			autoResize: true,
			height: '100%',
			width: '100%',
			interaction: { multiselect: true },
			nodes: { shadow: { enabled: true, size: 5 } },
			edges: { shadow: { enabled: true, size: 5 } }
		};
        $scope.$watch('file', function () {
        	$scope.upload($scope.file, 'model', 'status', true);
        	$scope.transactionStatus = '';
        });
        
        $scope.$watch('transactionsFile', function () {
        	$scope.upload($scope.transactionsFile, 'model/'+ $scope.modelId+'/transactions', 'transactionStatus', false);
        });

        $scope.$watch('separationCriteriaFile', function () {
        	$scope.upload($scope.separationCriteriaFile, 'model/'+ $scope.modelId+'/separationCriteria', 'separationStatus', false);
        });
        
        $scope.$watch('distancesFile', function () {
        	$scope.upload($scope.distancesFile, 'model/'+ $scope.modelId+'/distanceVariants', 'distanceStatus', false);
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
    			angular.forEach($scope.model.entities, function(entity) {
    				contextNodes.add({id: nodeId, label: entity.name});
    				entitiesById[entity.name] = nodeId;
    				$scope.modelsById[nodeId] = entity.name;
    				nodeId++;
    			});
    			var relationTypes = {
    					'Composition': {arrows: 'middle', width: 1},
    					'Aggregation': {arrows: 'middle', dashes: true, width: 1},
    					'Inheritance': {arrows: 'to', width: 1}};
    			
    			angular.forEach($scope.model['coupling'], function(relation) {
    				var relationStyle = null;
    				if(relationStyle = relationTypes[relation.variant.name]) {
    					var names = relation.name.split('.');
    					var from = names[0];
    					var to = names[1];
    					var edge = jQuery.extend({from: entitiesById[from], to: entitiesById[to], },relationStyle);
    					contextEdges.add(edge);
    				}
    			});
    	        $scope.graphData = {
	            	'nodes': contextNodes,
	            	'edges': contextEdges
	            };
        	}
        });

        $scope.selectNode = function(param) {
        	if(param.nodes.length > 0) {
        		var selectedNodes = param.nodes.map(function(node) { return $scope.modelsById[parseInt(node)]; });
        		$scope.$apply(function() {
        			$scope.model.filteredDataFields = $scope.model.dataFields.filter(function(field) {return selectedNodes.indexOf(field.context) >= 0;});
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
        			$scope.model.filteredDataFields = model.dataFields
        			$scope.loadCoupling($scope.modelId);
        		});
        	}
        };
        
        $scope.loadCoupling = function (id) {
        	Model.getCoupling({id:id}, function(coupling) {
        		$scope.model['coupling'] = coupling;
        		$scope.model['entities'] = coupling.filter(function(item) { return item.variant.name == 'Same Entity' ;});
        	});
        }
        
        $scope.listFieldNames = function (fields) {
        	return fields.map(function (o,i) {
        		return o.name;
        	}).join(', ');
        }
        
        
        $scope.transactionStatus = 'No upload yet.';
        $scope.separationStatus = 'No upload yet.';
        $scope.distanceStatus = 'No upload yet.';
        $scope.status = 'No upload yet.';
        $scope.modelId = 0;
        $scope.model = null;
        $scope.modelsById = {};
        $scope.availableModels = Model.all(function(models) {
        	$scope.status = 'Select a model or upload a new one.';
        });
    });
