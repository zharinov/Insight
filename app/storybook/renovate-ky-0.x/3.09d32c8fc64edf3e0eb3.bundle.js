(window.webpackJsonp=window.webpackJsonp||[]).push([[3],{927:function(module,__webpack_exports__,__webpack_require__){"use strict";__webpack_require__.r(__webpack_exports__),__webpack_require__.d(__webpack_exports__,"default",(function(){return Console_Console}));__webpack_require__(32),__webpack_require__(65),__webpack_require__(230);var react=__webpack_require__(0),react_default=__webpack_require__.n(react),Console=__webpack_require__(231),esm=__webpack_require__(58),api=__webpack_require__(67),ConsoleContainer=function(_ref){var sessionId=_ref.sessionId,data=Object(esm.a)("ConsoleContainer.sessions/".concat(sessionId,"/events/search"),(function(){return api.b.events.search(sessionId,{searchParams:{"event.e":["gte:9","lte:10"],limit:1e3}})}),{refreshWhenHidden:!0,refreshInterval:5e3}).data;return react_default.a.createElement(Console.a,{events:(data||[]).slice(0,50),loading:void 0===data})};ConsoleContainer.displayName="ConsoleContainer",ConsoleContainer.__docgenInfo={description:"",methods:[],displayName:"ConsoleContainer",props:{sessionId:{required:!0,tsType:{name:"string"},description:""}}};var Console_Console=react_default.a.memo(ConsoleContainer);"undefined"!=typeof STORYBOOK_REACT_CLASSES&&(STORYBOOK_REACT_CLASSES["src/modules/sessions/containers/Console/Console.tsx"]={name:"ConsoleContainer",docgenInfo:ConsoleContainer.__docgenInfo,path:"src/modules/sessions/containers/Console/Console.tsx"})}}]);
//# sourceMappingURL=3.09d32c8fc64edf3e0eb3.bundle.js.map