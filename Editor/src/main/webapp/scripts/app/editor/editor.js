'use strict';

angular.module('editorApp')
    .config(function ($stateProvider) {
        $stateProvider
            .state('editor', {
                parent: 'site',
                url: '/editor',
                data: {
                    authorities: []
                },
                views: {
                    'content@': {
                        templateUrl: 'scripts/app/editor/editor.html',
                        controller: 'EditorController'
                    }
                },
                resolve: { 
                    
                }
            });
    });
