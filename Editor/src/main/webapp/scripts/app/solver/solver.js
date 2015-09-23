'use strict';

angular.module('editorApp')
    .config(function ($stateProvider) {
        $stateProvider
            .state('solver', {
                parent: 'site',
                url: '/solver',
                data: {
                    authorities: []
                },
                views: {
                    'content@': {
                        templateUrl: 'scripts/app/solver/solver.html',
                        controller: 'SolverController'
                    }
                },
                resolve: {
                    
                }
            });
    });
