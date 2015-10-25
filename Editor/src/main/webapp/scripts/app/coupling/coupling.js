'use strict';

angular.module('editorApp')
    .config(function ($stateProvider) {
        $stateProvider
            .state('coupling', {
                parent: 'site',
                url: '/coupling',
                data: {
                    authorities: []
                },
                views: {
                    'content@': {
                        templateUrl: 'scripts/app/coupling/coupling.html',
                        controller: 'CouplingController'
                    }
                },
                resolve: {
                    
                }
            });
    });
