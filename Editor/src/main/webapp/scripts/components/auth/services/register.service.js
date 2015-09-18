'use strict';

angular.module('editorApp')
    .factory('Register', function ($resource) {
        return $resource('api/register', {}, {
        });
    });


