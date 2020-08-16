'use strict';

angular.module('editorApp')
    .controller('SolverController', function ($scope, $rootScope, $http, Principal, VisDataSet, Model, Coupling, Blob, FileSaver, $timeout) {
        Principal.identity().then(function(account) {
            $scope.account = account;
            $scope.isAuthenticated = Principal.isAuthenticated;
        });

        //introduce early to not show the alerts on init
        $scope.recalculationRequired = false,
		$scope.showGirvanWarning = false;

        /********* SOLVE ***********/

        $scope.$watch('modelId', function () {
        	if($scope.modelId != undefined){
	        	$scope.solve();
	        	$rootScope.modelId = $scope.modelId;
	        	$scope.showRelations = false;
        	}
        });

        $scope.$watch('algorithm', function () {
        	$scope.girvanNewmanActive = $scope.algorithm == 'Girvan-Newman';
        	$scope.leungActive = $scope.algorithm == 'Leung';
        	$scope.cwActive = $scope.algorithm == 'Chinese Whispers';
        	$scope.solve();
        });

        $scope.requireRecalculate=function(){
        	$scope.recalculationRequired  = true;
        }

        $scope.solve=function(){
        	$scope.selectedServiceName = '';
        	$scope.solveModel($scope.modelId);
        	$scope.recalculationRequired = false;
        }

        $scope.solveModel = function(modelId) {
        	if(parseInt(modelId) > 0) {
        		var solverConfig = {'algorithmParams': {'inflation': $scope.inflationSlider,
        										  'power': $scope.powerSlider,
        										  'prune': $scope.pruneSlider,
        										  'extraClusters': $scope.extraClusterSlider,
        										  'numberOfClusters': $scope.numberSlider,
        										  'leungM': $scope.leungM,
        										  'leungDelta': $scope.leungDelta,
        										  'mclExpansionOperations': $scope.mclExpansionOperations,
        										  'mclPowerCoefficient': $scope.mclPowerCoefficient,
        										  'cwNodeWeighting': $scope.cwNodeWeighting
        							},
        							'priorities': {},
        							'algorithm': $scope.algorithm
        		};
        		angular.forEach($scope.criteria, function(value, index){
        			if ('undefined' !== typeof value.priority) {
        				solverConfig.priorities[value.name] = parseFloat(value.priority)
        			}
        		})

        		$http.post('/api/engine/solver/' + modelId, solverConfig).
		    		success(function(data) {
		        		$scope.result = data;
		        		$scope.repaintGraph();
	            });
        	}
        }



        /****  EXPORTER  **********/

        $scope.downloadCut = function() {
        	var data = JSON.stringify($scope.result);
        	var data = new Blob([data], { type: 'text/plain;charset=utf-8' });
            FileSaver.saveAs(data, 'Services.json');
        }

        /****** ANALYSE MODE ********/

        $scope.removeSelection = function(param) {
    		$scope.selectedServiceName = (function () { return; })();
    		$scope.selectedServiceUseCases = (function () { return; })();
    		$scope.selectedServiceRelations = (function () { return; })();
        }

        $scope.updateSelection = function(param) {
        	$scope.$apply(function() {
        		$scope.removeSelection();
        	});
        	if(param && param.nodes.length > 0) {
        		var nodeId = param.nodes[0];
        		var listOfUseCases = $scope.result.useCaseResponsibility[nodeId];
        		if(listOfUseCases != undefined){
	        		$scope.$apply(function() { $scope.selectedServiceName = nodeId;});
	        		if(typeof listOfUseCases != 'undefined' && listOfUseCases.length > 0){
	        			$scope.$apply(function() { $scope.selectedServiceUseCases = listOfUseCases;});
	        		}
        		}
        		var selectedServiceRelations = [];
        		var relations = $scope.result.relations;
        		for(var relation in relations){
					if(relations[relation].serviceA == nodeId || relations[relation].serviceB == nodeId){
						var r = {};
						r['name'] = relations[relation].serviceA + ' - ' +  relations[relation].serviceB
						r['entities'] = relations[relation].sharedEntities;
						selectedServiceRelations.push(r);
					}
        		}
        		$scope.$apply(function() { $scope.selectedServiceRelations = selectedServiceRelations;});
    		}else if(param.edges.length > 0){
        		var edgeId = param.edges[0];
    			var relations = $scope.result.relations;
    			for(var relation in relations){
					if(edgeId.indexOf(relations[relation].serviceA) > -1 && edgeId.indexOf(relations[relation].serviceB) > -1){
		        		var selectedServiceRelations = [];
						var r = {};
						r['name'] = relations[relation].serviceA + ' - ' +  relations[relation].serviceB
						r['entities'] = relations[relation].sharedEntities;
						selectedServiceRelations.push(r);
		        		$scope.$apply(function() { $scope.selectedServiceRelations = selectedServiceRelations;});
		        		break;
					}
        		}
        	}
        };

        //watch for service relations
        $scope.$watch('result', function () {
        	var hasRelations = $scope.result && $scope.result.relations && $scope.result.relations.length > 0;
        	$scope.disableVisualization = $scope.result == undefined || ($scope.result.services.length > 1 && !hasRelations);
        });


        /********** VIS.JS GRAPH ************/
        $scope.graphOptions = {
			autoResize: true,
			height: '100%',
			width: '100%',
			nodes : {
				font: '19px arial black'
			}
		};

        $scope.graphResize = function(param) {
        	this.fit(); // Zooms out so all nodes fit on the canvas.
        };

        $scope.graphEvents = {
            	selectNode: $scope.updateSelection,
            	deselectNode: $scope.updateSelection,
            	deselectEdge: $scope.updateSelection,
            	selectEdge: $scope.updateSelection,
            	resize: $scope.graphResize
        };

        $scope.repaintGraph = function(){
			var serviceNodes = new VisDataSet([]);
			var serviceEdges = new VisDataSet([]);
			var nodeId = 1;
			var services = $scope.result.services;
			// services
			for (var x in services) {
				serviceNodes.add({id: services[x].name, shape: 'square', color: '#93D276', label: services[x].name});
				for (var y in services[x].nanoentities) {
					var nanoentity = services[x].nanoentities[y];
					serviceNodes.add({id: nodeId, shape: 'square', size: 10, color: '#909090', label: nanoentity});
					serviceEdges.add({from: services[x].name, to: nodeId});
					nodeId++;
				}
			}
			// service relations
			if ($scope.showRelations){
    			for (var relation in $scope.result.relations){
    					var relation = $scope.result.relations[relation];
    					var fromNode = relation.serviceA;
    					var toNode = relation.serviceB;
    					var a = '';
    					if (relation.direction == "BIDIRECTIONAL") {
    						a = 'to, from';
    					} else if (relation.direction == "INCOMING") {
    						a = 'from';
    					} else if (relation.direction == "OUTGOING") {
    						a = 'to';
    					}
    					var edge = {from: fromNode, to: toNode, id: fromNode + '-'+toNode, color:'#B0DF9B', arrows: a};
    					serviceEdges.add(edge);
    			}
			}
	        $scope.graphData = {
            	'nodes': serviceNodes,
            	'edges': serviceEdges
            };
	        $scope.removeSelection();
        }




        /***** MISC ***********/

        $scope.expectComparator = function (actual, expected) {
            if (!expected) {
               return true;
            }
            return angular.equals(expected, actual);
        }

        // watcher for girvanNewmanWarningNoC
        $scope.$watch('result', function () {
			$scope.showGirvanWarningNoC = false;
        	if($scope.result && $scope.girvanNewmanActive == true){
				if($scope.numberSlider != $scope.result.services.length){
					$scope.showGirvanWarningNoC = true;
				}
        	}
        });

        // watcher for girvanNewmanWarningNoClustering
        $scope.$watch('result', function () {
        	$scope.showGirvanWarning = false;
        	var oneNanoEntityPerService = true;

        	if($scope.result && $scope.girvanNewmanActive == true){
	        	var services = $scope.result.services;
				// services
				for (var x in services) {
					if(services[x].nanoentities.length > 1){
						oneNanoEntityPerService = false;
					}
				}

				$scope.showGirvanWarning = oneNanoEntityPerService;
        	}
        });

        /***** DEFINITIONS ***********/

        $scope.priorityMetric = {
        		IGNORE : {value: 0, name: "IGNORE"},
        		XS : {value: 0.5, name: "XS"},
        		S : {value: 1, name: "S"},
        		M: {value: 3, name: "M"},
        		L : {value: 5, name: "L"},
        		XL : {value: 8, name: "XL"},
        		XXL : {value: 13, name: "XXL"}
        }
        $scope.availableAlgorithms = ['Leung','Girvan-Newman','Chinese Whispers','Markov (MCL)'];

        $scope.criteriaTypes = ["COHESIVENESS", "COMPATIBILITY", "CONSTRAINTS"];


        /***** DEFAULTS ***********/
        // init priorities, algorithm and then solve if global models have been defined
        $scope.criteria = Coupling.all(function(criteria) {
        	angular.forEach(criteria, function(value, index){
        		if (value.type == "COHESIVENESS" || value.type == "CONSTRAINTS"){
        			value.priority = 3 // todo refactor, move to server?
        		} else{
        			value.priority = 0.5
        		}
        	})


        	$scope.algorithm = 'Girvan-Newman'; // default
        	$scope.modelId = $rootScope.modelId;
        });

        $scope.solved = false;

		$scope.powerSlider = 1;
		$scope.inflationSlider = 2;
		$scope.pruneSlider = 0.0;
		$scope.extraClusterSlider = 0;
		$scope.numberSlider = 3;
		$scope.showRelations = false;

		$scope.leungM = 0.1;
		$scope.leungDelta = 0.55;

		$scope.mclExpansionOperations = 2;
		$scope.mclPowerCoefficient = 2;

		$scope.cwNodeWeighting = 0;

        $scope.availableModels = Model.all();

    }).filter('capitalize', function() {
        return function(txt){return txt.charAt(0).toUpperCase() + txt.substr(1).toLowerCase();}
        });
