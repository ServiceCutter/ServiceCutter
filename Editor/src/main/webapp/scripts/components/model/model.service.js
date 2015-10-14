'use strict';

angular.module('editorApp')
    .factory('Model', function ($resource) {
        return $resource('api/engine/models/:id', {modelId: '@id'}, {
                all: {
                	url: 'api/engine/models',
                	method: 'GET',
                	isArray: true
                },
                get: {
                	method: 'GET'
                },
                getCoupling: {
                	method: 'GET',
                	url: 'api/engine/models/:id/couplingcriteria',
                	isArray: true
                }
            });
        });
