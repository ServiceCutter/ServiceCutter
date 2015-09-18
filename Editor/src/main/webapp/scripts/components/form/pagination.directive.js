/* globals $ */
'use strict';

angular.module('editorApp')
    .directive('editorAppPagination', function() {
        return {
            templateUrl: 'scripts/components/form/pagination.html'
        };
    });
