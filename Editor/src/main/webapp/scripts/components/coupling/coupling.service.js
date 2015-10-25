'use strict';

angular.module('editorApp')
    .factory('Coupling', function ($resource) {
        return $resource('api/engine/couplingcriteria', {}, {
                all: {
                	url: 'api/engine/couplingcriteria',
                	method: 'GET',
                	isArray: true
                }
            });
        });
