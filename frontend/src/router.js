
import Vue from 'vue'
import Router from 'vue-router'

Vue.use(Router);


import ConferenceManager from "./components/ConferenceManager"

import PayManager from "./components/PayManager"

import RoomManager from "./components/RoomManager"


import RoomState from "./components/RoomState"
export default new Router({
    // mode: 'history',
    base: process.env.BASE_URL,
    routes: [
            {
                path: '/Conference',
                name: 'ConferenceManager',
                component: ConferenceManager
            },

            {
                path: '/Pay',
                name: 'PayManager',
                component: PayManager
            },

            {
                path: '/Room',
                name: 'RoomManager',
                component: RoomManager
            },


            {
                path: '/RoomState',
                name: 'RoomState',
                component: RoomState
            },


    ]
})
