'use strict';

angular.module('editorApp')
    .factory('Model', function ($resource) {
        return $resource('api/engine/systems/:id', {modelId: '@id'}, {
                all: {
                	url: 'api/engine/systems',
                	method: 'GET',
                	isArray: true
                },
                get: {
                	method: 'GET'
                },
                delete: {
                	method: 'DELETE'
                },
                getCoupling: {
                	method: 'GET',
                	url: 'api/engine/:id/couplingdata',
                	isArray: true
                }
            });
        });
