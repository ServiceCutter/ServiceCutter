'use strict';

angular.module('editorApp')
    .controller('LogoutController', function (Auth) {
        Auth.logout();
    });
