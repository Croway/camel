/**
 * @license
 * Copyright 2019 Google LLC
 * SPDX-License-Identifier: BSD-3-Clause
 */
const t=globalThis,e=t.ShadowRoot&&(void 0===t.ShadyCSS||t.ShadyCSS.nativeShadow)&&"adoptedStyleSheets"in Document.prototype&&"replace"in CSSStyleSheet.prototype,s=Symbol(),i=new WeakMap;let n=class{constructor(t,e,i){if(this._$cssResult$=!0,i!==s)throw Error("CSSResult is not constructable. Use `unsafeCSS` or `css` instead.");this.cssText=t,this.t=e}get styleSheet(){let t=this.o;const s=this.t;if(e&&void 0===t){const e=void 0!==s&&1===s.length;e&&(t=i.get(s)),void 0===t&&((this.o=t=new CSSStyleSheet).replaceSync(this.cssText),e&&i.set(s,t))}return t}toString(){return this.cssText}};const o=(t,...e)=>{const i=1===t.length?t[0]:e.reduce((e,s,i)=>e+(t=>{if(!0===t._$cssResult$)return t.cssText;if("number"==typeof t)return t;throw Error("Value passed to 'css' function must be a 'css' function result: "+t+". Use 'unsafeCSS' to pass non-literal values, but take care to ensure page security.")})(s)+t[i+1],t[0]);return new n(i,t,s)},r=e?t=>t:t=>t instanceof CSSStyleSheet?(t=>{let e="";for(const s of t.cssRules)e+=s.cssText;return(t=>new n("string"==typeof t?t:t+"",void 0,s))(e)})(t):t,{is:a,defineProperty:l,getOwnPropertyDescriptor:h,getOwnPropertyNames:c,getOwnPropertySymbols:d,getPrototypeOf:u}=Object,p=globalThis,f=p.trustedTypes,$=f?f.emptyScript:"",m=p.reactiveElementPolyfillSupport,g=(t,e)=>t,y={toAttribute(t,e){switch(e){case Boolean:t=t?$:null;break;case Object:case Array:t=null==t?t:JSON.stringify(t)}return t},fromAttribute(t,e){let s=t;switch(e){case Boolean:s=null!==t;break;case Number:s=null===t?null:Number(t);break;case Object:case Array:try{s=JSON.parse(t)}catch(t){s=null}}return s}},_=(t,e)=>!a(t,e),x={attribute:!0,type:String,converter:y,reflect:!1,useDefault:!1,hasChanged:_};
/**
 * @license
 * Copyright 2017 Google LLC
 * SPDX-License-Identifier: BSD-3-Clause
 */Symbol.metadata??=Symbol("metadata"),p.litPropertyMetadata??=new WeakMap;let v=class extends HTMLElement{static addInitializer(t){this._$Ei(),(this.l??=[]).push(t)}static get observedAttributes(){return this.finalize(),this._$Eh&&[...this._$Eh.keys()]}static createProperty(t,e=x){if(e.state&&(e.attribute=!1),this._$Ei(),this.prototype.hasOwnProperty(t)&&((e=Object.create(e)).wrapped=!0),this.elementProperties.set(t,e),!e.noAccessor){const s=Symbol(),i=this.getPropertyDescriptor(t,s,e);void 0!==i&&l(this.prototype,t,i)}}static getPropertyDescriptor(t,e,s){const{get:i,set:n}=h(this.prototype,t)??{get(){return this[e]},set(t){this[e]=t}};return{get:i,set(e){const o=i?.call(this);n?.call(this,e),this.requestUpdate(t,o,s)},configurable:!0,enumerable:!0}}static getPropertyOptions(t){return this.elementProperties.get(t)??x}static _$Ei(){if(this.hasOwnProperty(g("elementProperties")))return;const t=u(this);t.finalize(),void 0!==t.l&&(this.l=[...t.l]),this.elementProperties=new Map(t.elementProperties)}static finalize(){if(this.hasOwnProperty(g("finalized")))return;if(this.finalized=!0,this._$Ei(),this.hasOwnProperty(g("properties"))){const t=this.properties,e=[...c(t),...d(t)];for(const s of e)this.createProperty(s,t[s])}const t=this[Symbol.metadata];if(null!==t){const e=litPropertyMetadata.get(t);if(void 0!==e)for(const[t,s]of e)this.elementProperties.set(t,s)}this._$Eh=new Map;for(const[t,e]of this.elementProperties){const s=this._$Eu(t,e);void 0!==s&&this._$Eh.set(s,t)}this.elementStyles=this.finalizeStyles(this.styles)}static finalizeStyles(t){const e=[];if(Array.isArray(t)){const s=new Set(t.flat(1/0).reverse());for(const t of s)e.unshift(r(t))}else void 0!==t&&e.push(r(t));return e}static _$Eu(t,e){const s=e.attribute;return!1===s?void 0:"string"==typeof s?s:"string"==typeof t?t.toLowerCase():void 0}constructor(){super(),this._$Ep=void 0,this.isUpdatePending=!1,this.hasUpdated=!1,this._$Em=null,this._$Ev()}_$Ev(){this._$ES=new Promise(t=>this.enableUpdating=t),this._$AL=new Map,this._$E_(),this.requestUpdate(),this.constructor.l?.forEach(t=>t(this))}addController(t){(this._$EO??=new Set).add(t),void 0!==this.renderRoot&&this.isConnected&&t.hostConnected?.()}removeController(t){this._$EO?.delete(t)}_$E_(){const t=new Map,e=this.constructor.elementProperties;for(const s of e.keys())this.hasOwnProperty(s)&&(t.set(s,this[s]),delete this[s]);t.size>0&&(this._$Ep=t)}createRenderRoot(){const s=this.shadowRoot??this.attachShadow(this.constructor.shadowRootOptions);return((s,i)=>{if(e)s.adoptedStyleSheets=i.map(t=>t instanceof CSSStyleSheet?t:t.styleSheet);else for(const e of i){const i=document.createElement("style"),n=t.litNonce;void 0!==n&&i.setAttribute("nonce",n),i.textContent=e.cssText,s.appendChild(i)}})(s,this.constructor.elementStyles),s}connectedCallback(){this.renderRoot??=this.createRenderRoot(),this.enableUpdating(!0),this._$EO?.forEach(t=>t.hostConnected?.())}enableUpdating(t){}disconnectedCallback(){this._$EO?.forEach(t=>t.hostDisconnected?.())}attributeChangedCallback(t,e,s){this._$AK(t,s)}_$ET(t,e){const s=this.constructor.elementProperties.get(t),i=this.constructor._$Eu(t,s);if(void 0!==i&&!0===s.reflect){const n=(void 0!==s.converter?.toAttribute?s.converter:y).toAttribute(e,s.type);this._$Em=t,null==n?this.removeAttribute(i):this.setAttribute(i,n),this._$Em=null}}_$AK(t,e){const s=this.constructor,i=s._$Eh.get(t);if(void 0!==i&&this._$Em!==i){const t=s.getPropertyOptions(i),n="function"==typeof t.converter?{fromAttribute:t.converter}:void 0!==t.converter?.fromAttribute?t.converter:y;this._$Em=i;const o=n.fromAttribute(e,t.type);this[i]=o??this._$Ej?.get(i)??o,this._$Em=null}}requestUpdate(t,e,s,i=!1,n){if(void 0!==t){const o=this.constructor;if(!1===i&&(n=this[t]),s??=o.getPropertyOptions(t),!((s.hasChanged??_)(n,e)||s.useDefault&&s.reflect&&n===this._$Ej?.get(t)&&!this.hasAttribute(o._$Eu(t,s))))return;this.C(t,e,s)}!1===this.isUpdatePending&&(this._$ES=this._$EP())}C(t,e,{useDefault:s,reflect:i,wrapped:n},o){s&&!(this._$Ej??=new Map).has(t)&&(this._$Ej.set(t,o??e??this[t]),!0!==n||void 0!==o)||(this._$AL.has(t)||(this.hasUpdated||s||(e=void 0),this._$AL.set(t,e)),!0===i&&this._$Em!==t&&(this._$Eq??=new Set).add(t))}async _$EP(){this.isUpdatePending=!0;try{await this._$ES}catch(t){Promise.reject(t)}const t=this.scheduleUpdate();return null!=t&&await t,!this.isUpdatePending}scheduleUpdate(){return this.performUpdate()}performUpdate(){if(!this.isUpdatePending)return;if(!this.hasUpdated){if(this.renderRoot??=this.createRenderRoot(),this._$Ep){for(const[t,e]of this._$Ep)this[t]=e;this._$Ep=void 0}const t=this.constructor.elementProperties;if(t.size>0)for(const[e,s]of t){const{wrapped:t}=s,i=this[e];!0!==t||this._$AL.has(e)||void 0===i||this.C(e,void 0,s,i)}}let t=!1;const e=this._$AL;try{t=this.shouldUpdate(e),t?(this.willUpdate(e),this._$EO?.forEach(t=>t.hostUpdate?.()),this.update(e)):this._$EM()}catch(e){throw t=!1,this._$EM(),e}t&&this._$AE(e)}willUpdate(t){}_$AE(t){this._$EO?.forEach(t=>t.hostUpdated?.()),this.hasUpdated||(this.hasUpdated=!0,this.firstUpdated(t)),this.updated(t)}_$EM(){this._$AL=new Map,this.isUpdatePending=!1}get updateComplete(){return this.getUpdateComplete()}getUpdateComplete(){return this._$ES}shouldUpdate(t){return!0}update(t){this._$Eq&&=this._$Eq.forEach(t=>this._$ET(t,this[t])),this._$EM()}updated(t){}firstUpdated(t){}};v.elementStyles=[],v.shadowRootOptions={mode:"open"},v[g("elementProperties")]=new Map,v[g("finalized")]=new Map,m?.({ReactiveElement:v}),(p.reactiveElementVersions??=[]).push("2.1.2");
/**
 * @license
 * Copyright 2017 Google LLC
 * SPDX-License-Identifier: BSD-3-Clause
 */
const b=globalThis,w=t=>t,A=b.trustedTypes,N=A?A.createPolicy("lit-html",{createHTML:t=>t}):void 0,S="$lit$",E=`lit$${Math.random().toFixed(9).slice(2)}$`,M="?"+E,T=`<${M}>`,C=document,P=()=>C.createComment(""),I=t=>null===t||"object"!=typeof t&&"function"!=typeof t,k=Array.isArray,O="[ \t\n\f\r]",U=/<(?:(!--|\/[^a-zA-Z])|(\/?[a-zA-Z][^>\s]*)|(\/?$))/g,Y=/-->/g,L=/>/g,H=RegExp(`>|${O}(?:([^\\s"'>=/]+)(${O}*=${O}*(?:[^ \t\n\f\r"'\`<>=]|("|')|))|$)`,"g"),B=/'/g,z=/"/g,R=/^(?:script|style|textarea|title)$/i,X=t=>(e,...s)=>({_$litType$:t,strings:e,values:s}),W=X(1),D=X(2),F=Symbol.for("lit-noChange"),j=Symbol.for("lit-nothing"),V=new WeakMap,Z=C.createTreeWalker(C,129);function q(t,e){if(!k(t)||!t.hasOwnProperty("raw"))throw Error("invalid template strings array");return void 0!==N?N.createHTML(e):e}const J=(t,e)=>{const s=t.length-1,i=[];let n,o=2===e?"<svg>":3===e?"<math>":"",r=U;for(let e=0;e<s;e++){const s=t[e];let a,l,h=-1,c=0;for(;c<s.length&&(r.lastIndex=c,l=r.exec(s),null!==l);)c=r.lastIndex,r===U?"!--"===l[1]?r=Y:void 0!==l[1]?r=L:void 0!==l[2]?(R.test(l[2])&&(n=RegExp("</"+l[2],"g")),r=H):void 0!==l[3]&&(r=H):r===H?">"===l[0]?(r=n??U,h=-1):void 0===l[1]?h=-2:(h=r.lastIndex-l[2].length,a=l[1],r=void 0===l[3]?H:'"'===l[3]?z:B):r===z||r===B?r=H:r===Y||r===L?r=U:(r=H,n=void 0);const d=r===H&&t[e+1].startsWith("/>")?" ":"";o+=r===U?s+T:h>=0?(i.push(a),s.slice(0,h)+S+s.slice(h)+E+d):s+E+(-2===h?e:d)}return[q(t,o+(t[s]||"<?>")+(2===e?"</svg>":3===e?"</math>":"")),i]};class K{constructor({strings:t,_$litType$:e},s){let i;this.parts=[];let n=0,o=0;const r=t.length-1,a=this.parts,[l,h]=J(t,e);if(this.el=K.createElement(l,s),Z.currentNode=this.el.content,2===e||3===e){const t=this.el.content.firstChild;t.replaceWith(...t.childNodes)}for(;null!==(i=Z.nextNode())&&a.length<r;){if(1===i.nodeType){if(i.hasAttributes())for(const t of i.getAttributeNames())if(t.endsWith(S)){const e=h[o++],s=i.getAttribute(t).split(E),r=/([.?@])?(.*)/.exec(e);a.push({type:1,index:n,name:r[2],strings:s,ctor:"."===r[1]?st:"?"===r[1]?it:"@"===r[1]?nt:et}),i.removeAttribute(t)}else t.startsWith(E)&&(a.push({type:6,index:n}),i.removeAttribute(t));if(R.test(i.tagName)){const t=i.textContent.split(E),e=t.length-1;if(e>0){i.textContent=A?A.emptyScript:"";for(let s=0;s<e;s++)i.append(t[s],P()),Z.nextNode(),a.push({type:2,index:++n});i.append(t[e],P())}}}else if(8===i.nodeType)if(i.data===M)a.push({type:2,index:n});else{let t=-1;for(;-1!==(t=i.data.indexOf(E,t+1));)a.push({type:7,index:n}),t+=E.length-1}n++}}static createElement(t,e){const s=C.createElement("template");return s.innerHTML=t,s}}function G(t,e,s=t,i){if(e===F)return e;let n=void 0!==i?s._$Co?.[i]:s._$Cl;const o=I(e)?void 0:e._$litDirective$;return n?.constructor!==o&&(n?._$AO?.(!1),void 0===o?n=void 0:(n=new o(t),n._$AT(t,s,i)),void 0!==i?(s._$Co??=[])[i]=n:s._$Cl=n),void 0!==n&&(e=G(t,n._$AS(t,e.values),n,i)),e}class Q{constructor(t,e){this._$AV=[],this._$AN=void 0,this._$AD=t,this._$AM=e}get parentNode(){return this._$AM.parentNode}get _$AU(){return this._$AM._$AU}u(t){const{el:{content:e},parts:s}=this._$AD,i=(t?.creationScope??C).importNode(e,!0);Z.currentNode=i;let n=Z.nextNode(),o=0,r=0,a=s[0];for(;void 0!==a;){if(o===a.index){let e;2===a.type?e=new tt(n,n.nextSibling,this,t):1===a.type?e=new a.ctor(n,a.name,a.strings,this,t):6===a.type&&(e=new ot(n,this,t)),this._$AV.push(e),a=s[++r]}o!==a?.index&&(n=Z.nextNode(),o++)}return Z.currentNode=C,i}p(t){let e=0;for(const s of this._$AV)void 0!==s&&(void 0!==s.strings?(s._$AI(t,s,e),e+=s.strings.length-2):s._$AI(t[e])),e++}}class tt{get _$AU(){return this._$AM?._$AU??this._$Cv}constructor(t,e,s,i){this.type=2,this._$AH=j,this._$AN=void 0,this._$AA=t,this._$AB=e,this._$AM=s,this.options=i,this._$Cv=i?.isConnected??!0}get parentNode(){let t=this._$AA.parentNode;const e=this._$AM;return void 0!==e&&11===t?.nodeType&&(t=e.parentNode),t}get startNode(){return this._$AA}get endNode(){return this._$AB}_$AI(t,e=this){t=G(this,t,e),I(t)?t===j||null==t||""===t?(this._$AH!==j&&this._$AR(),this._$AH=j):t!==this._$AH&&t!==F&&this._(t):void 0!==t._$litType$?this.$(t):void 0!==t.nodeType?this.T(t):(t=>k(t)||"function"==typeof t?.[Symbol.iterator])(t)?this.k(t):this._(t)}O(t){return this._$AA.parentNode.insertBefore(t,this._$AB)}T(t){this._$AH!==t&&(this._$AR(),this._$AH=this.O(t))}_(t){this._$AH!==j&&I(this._$AH)?this._$AA.nextSibling.data=t:this.T(C.createTextNode(t)),this._$AH=t}$(t){const{values:e,_$litType$:s}=t,i="number"==typeof s?this._$AC(t):(void 0===s.el&&(s.el=K.createElement(q(s.h,s.h[0]),this.options)),s);if(this._$AH?._$AD===i)this._$AH.p(e);else{const t=new Q(i,this),s=t.u(this.options);t.p(e),this.T(s),this._$AH=t}}_$AC(t){let e=V.get(t.strings);return void 0===e&&V.set(t.strings,e=new K(t)),e}k(t){k(this._$AH)||(this._$AH=[],this._$AR());const e=this._$AH;let s,i=0;for(const n of t)i===e.length?e.push(s=new tt(this.O(P()),this.O(P()),this,this.options)):s=e[i],s._$AI(n),i++;i<e.length&&(this._$AR(s&&s._$AB.nextSibling,i),e.length=i)}_$AR(t=this._$AA.nextSibling,e){for(this._$AP?.(!1,!0,e);t!==this._$AB;){const e=w(t).nextSibling;w(t).remove(),t=e}}setConnected(t){void 0===this._$AM&&(this._$Cv=t,this._$AP?.(t))}}class et{get tagName(){return this.element.tagName}get _$AU(){return this._$AM._$AU}constructor(t,e,s,i,n){this.type=1,this._$AH=j,this._$AN=void 0,this.element=t,this.name=e,this._$AM=i,this.options=n,s.length>2||""!==s[0]||""!==s[1]?(this._$AH=Array(s.length-1).fill(new String),this.strings=s):this._$AH=j}_$AI(t,e=this,s,i){const n=this.strings;let o=!1;if(void 0===n)t=G(this,t,e,0),o=!I(t)||t!==this._$AH&&t!==F,o&&(this._$AH=t);else{const i=t;let r,a;for(t=n[0],r=0;r<n.length-1;r++)a=G(this,i[s+r],e,r),a===F&&(a=this._$AH[r]),o||=!I(a)||a!==this._$AH[r],a===j?t=j:t!==j&&(t+=(a??"")+n[r+1]),this._$AH[r]=a}o&&!i&&this.j(t)}j(t){t===j?this.element.removeAttribute(this.name):this.element.setAttribute(this.name,t??"")}}class st extends et{constructor(){super(...arguments),this.type=3}j(t){this.element[this.name]=t===j?void 0:t}}class it extends et{constructor(){super(...arguments),this.type=4}j(t){this.element.toggleAttribute(this.name,!!t&&t!==j)}}class nt extends et{constructor(t,e,s,i,n){super(t,e,s,i,n),this.type=5}_$AI(t,e=this){if((t=G(this,t,e,0)??j)===F)return;const s=this._$AH,i=t===j&&s!==j||t.capture!==s.capture||t.once!==s.once||t.passive!==s.passive,n=t!==j&&(s===j||i);i&&this.element.removeEventListener(this.name,this,s),n&&this.element.addEventListener(this.name,this,t),this._$AH=t}handleEvent(t){"function"==typeof this._$AH?this._$AH.call(this.options?.host??this.element,t):this._$AH.handleEvent(t)}}class ot{constructor(t,e,s){this.element=t,this.type=6,this._$AN=void 0,this._$AM=e,this.options=s}get _$AU(){return this._$AM._$AU}_$AI(t){G(this,t)}}const rt=b.litHtmlPolyfillSupport;rt?.(K,tt),(b.litHtmlVersions??=[]).push("3.3.3");const at=globalThis;
/**
 * @license
 * Copyright 2017 Google LLC
 * SPDX-License-Identifier: BSD-3-Clause
 */class lt extends v{constructor(){super(...arguments),this.renderOptions={host:this},this._$Do=void 0}createRenderRoot(){const t=super.createRenderRoot();return this.renderOptions.renderBefore??=t.firstChild,t}update(t){const e=this.render();this.hasUpdated||(this.renderOptions.isConnected=this.isConnected),super.update(t),this._$Do=((t,e,s)=>{const i=s?.renderBefore??e;let n=i._$litPart$;if(void 0===n){const t=s?.renderBefore??null;i._$litPart$=n=new tt(e.insertBefore(P(),t),t,void 0,s??{})}return n._$AI(t),n})(e,this.renderRoot,this.renderOptions)}connectedCallback(){super.connectedCallback(),this._$Do?.setConnected(!0)}disconnectedCallback(){super.disconnectedCallback(),this._$Do?.setConnected(!1)}render(){return F}}lt._$litElement$=!0,lt.finalized=!0,at.litElementHydrateSupport?.({LitElement:lt});const ht=at.litElementPolyfillSupport;ht?.({LitElement:lt}),(at.litElementVersions??=[]).push("4.2.2");const ct=30,dt=14,ut=new Set(["choice","multicast","doTry","loadBalance","recipientList","circuitBreaker"]),pt=new Set(["when","otherwise","doCatch","doFinally","onFallback"]),ft=new Set(["route","from"]);function $t(t){return null!=t&&ut.has(t)}function mt(t){return null!=t.parent&&t.children.length>0&&!pt.has(t.info.type)&&!ft.has(t.info.type)}function gt(t,e,s){if(0===t.children.length)return t.subtreeWidth=e,t.subtreeWidth;if($t(t.info.type)){let i=0;for(let n=0;n<t.children.length;n++)n>0&&(i+=s),i+=gt(t.children[n],e,s);t.subtreeWidth=Math.max(e,i)}else{let i=e;for(const n of t.children)i=Math.max(i,gt(n,e,s));t.subtreeWidth=i}return t.subtreeWidth}function yt(t,e,s){if(mt(t)){const i={minX:t.layoutNode.x,minY:t.layoutNode.y,maxX:t.layoutNode.x+s,maxY:t.layoutNode.y+t.layoutNode.height};for(const e of t.children)yt(e,i,s);e.minX=Math.min(e.minX,i.minX-dt),e.minY=Math.min(e.minY,i.minY-dt),e.maxX=Math.max(e.maxX,i.maxX+dt),e.maxY=Math.max(e.maxY,i.maxY+dt)}else{null!=t.layoutNode&&(e.minX=Math.min(e.minX,t.layoutNode.x),e.minY=Math.min(e.minY,t.layoutNode.y),e.maxX=Math.max(e.maxX,t.layoutNode.x+s),e.maxY=Math.max(e.maxY,t.layoutNode.y+t.layoutNode.height));for(const i of t.children)yt(i,e,s)}}function _t(t){return 0===t.children.length||$t(t.info.type)?t.layoutNode:_t(t.children[t.children.length-1])}function xt(t){let e=null!=t.layoutNode?t.layoutNode.y+t.layoutNode.height:0;for(const s of t.children)e=Math.max(e,xt(s));return e}function vt(t,e,s,i,n,o,r,a,l,h,c){const d=Math.max(t.subtreeWidth,i),u=e+(d-o)/2,p=function(t,e){const s=function(t){return null==t?"":t.replace(/^\./,"")}(t.code),i=null!=t.description&&t.description.trim().length>0;switch(e){case"description":return[i?t.description:s];case"both":return i&&t.description!==s?[t.description,s]:[s];default:return[s]}}(t.info,c).flatMap(t=>function(t,e,s,i){const n=e-s,o=function(t){return.6*t}(i);if(t.length*o<=n)return[t];const r=[];let a=t;for(;a.length>0&&r.length<3;){if(a.length*o<=n){r.push(a),a="";break}let t=-1;const e=Math.floor(n/o);for(let s=0;s<Math.min(a.length,e+1);s++){const e=a[s];" "!==e&&":"!==e&&"/"!==e&&"."!==e&&","!==e&&"&"!==e&&"?"!==e||(t=s+1)}t<=0&&(t=Math.min(e,a.length),t<=0&&(t=1)),r.push(a.substring(0,t)),a=a.substring(t).trimStart()}if(a.length>0){const t=r.length-1;r[t]=r[t]+a}return r}(t,o,a,l)),f=function(t,e,s){const i=Math.min(t.length,3);return i<=1?e:e+1.4*s*(i-1)}(p,h,l),$={type:t.info.type,id:t.info.id,x:u,y:s,height:f,wrappedLines:p,parentNode:null,treeNode:t,connectFromMerge:!1,mergeY:0,mergeCx:0};if(t.layoutNode=$,n.nodes.push($),null!=t.parent&&null!=t.parent.layoutNode){const e=t.parent;if($t(e.info.type))$.parentNode=e.layoutNode;else{const s=e.children.indexOf(t);if(s>0){const t=e.children[s-1];if(mt(t)){$.connectFromMerge=!0;const e={minX:t.layoutNode.x,minY:t.layoutNode.y,maxX:t.layoutNode.x+o,maxY:t.layoutNode.y+t.layoutNode.height};for(const s of t.children)yt(s,e,o);$.mergeY=e.maxY+dt,$.mergeCx=t.layoutNode.x+o/2,$.parentNode=t.layoutNode}else $.parentNode=_t(t)}else $.parentNode=e.layoutNode}}if(n.maxY=Math.max(n.maxY,s+$.height),0===t.children.length)return;const m=s+$.height+40;if($t(t.info.type)){let s=e+(d-t.subtreeWidth)/2;for(const e of t.children){let t=m;e.children.length>0&&!pt.has(e.info.type)&&(t+=dt),vt(e,s,t,e.subtreeWidth,n,o,r,a,l,h,c),s+=e.subtreeWidth+r}}else{let s=m;for(let i=0;i<t.children.length;i++){const u=t.children[i];if(vt(u,e,mt(u)?s+dt:s,d,n,o,r,a,l,h,c),mt(u)){const t={minX:u.layoutNode.x,minY:u.layoutNode.y,maxX:u.layoutNode.x+o,maxY:u.layoutNode.y+u.layoutNode.height};for(const e of u.children)yt(e,t,o);s=t.maxY+dt+40}else s=xt(u)+40}}}function bt(t,e,s={}){const i=s.nodeWidth||180,n=s.fontSize||12,o=s.nodeLabel||"code",r=Math.max(i,80),a=Math.floor(r/2),l=Math.max(Math.floor(16*r/180),8),h=Math.max(32,Math.floor(32*n/12)),c={routeId:t.routeId,source:t.source,labelY:e,maxX:0,maxY:0,nodes:[]},d=function(t){if(!t||0===t.length)return null;const e={info:t[0],parent:null,children:[],subtreeWidth:0,layoutNode:null};let s=e;for(let i=1;i<t.length;i++){const n=t[i],o={info:n,parent:null,children:[],subtreeWidth:0,layoutNode:null};if(n.level>s.info.level)s.children.push(o),o.parent=s;else if(n.level===s.info.level){const t=s.parent;null!=t?(t.children.push(o),o.parent=t):(e.children.push(o),o.parent=e)}else{let t=s.parent;for(;null!=t&&t.info.level>=n.level;)t=t.parent;null!=t?(t.children.push(o),o.parent=t):(e.children.push(o),o.parent=e)}s=o}return e}(t.nodes);if(null==d)return c.maxX=ct+r,c.maxY=e+24,c;gt(d,r,a),vt(d,ct,e+24,d.subtreeWidth,c,r,a,l,n,h,o);const u={minX:d.layoutNode.x,minY:d.layoutNode.y,maxX:d.layoutNode.x+r,maxY:d.layoutNode.y+d.layoutNode.height};for(const t of d.children)yt(t,u,r);if(u.minX<ct){const t=ct-u.minX;for(const e of c.nodes)e.x+=t,e.connectFromMerge&&(e.mergeCx+=t);u.maxX+=t}return c.maxX=u.maxX,c.maxY=Math.max(c.maxY,u.maxY),c}function wt(t){if(!t||0===t.trim().length)return null;if((t=t.replace(/ /g,"-")).startsWith("source:")){const e=(t=t.substring(7)).lastIndexOf(".");e>=0&&(t=t.substring(e+1))}const e=t.indexOf("://");e>=0&&(t=t.substring(e+3));const s=t.lastIndexOf(":");if(s>=0){const e=t.substring(s+1);/^\d+$/.test(e)&&(t=t.substring(0,s))}const i=t.lastIndexOf("/");i>=0&&(t=t.substring(i+1));const n=t.lastIndexOf("\\");return n>=0&&(t=t.substring(n+1)),t}const At={bg:"#0d1117",text:"#f0f6fc",arrow:"#656c76",label:"#d1d7e0",counterOk:"#2e7d32",counterFail:"#ff0000",from:"#238636",to:"#1f6feb",eip:"#8957e5",choice:"#d29922",default:"#3d444d",transform:"#1b7c83",processor:"#bf4b8a",selectedStroke:"#58a6ff",badgeBg:"#d1d7e0",badgeText:"#0d1117",tooltipBg:"#161b22",tooltipText:"#e6edf3",tooltipBorder:"#30363d"},Nt={bg:"#f6f8fa",text:"#f0f6fc",arrow:"#59636e",label:"#1f2328",counterOk:"#2e7d32",counterFail:"#d32f2f",from:"#238636",to:"#1f6feb",eip:"#8957e5",choice:"#d29922",default:"#3d444d",transform:"#1b7c83",processor:"#bf4b8a",selectedStroke:"#0969da",badgeBg:"#59636e",badgeText:"#f6f8fa",tooltipBg:"#f6f8fa",tooltipText:"#1f2328",tooltipBorder:"#d1d7e0"};const St=new Set(["when","otherwise","doCatch","doFinally","onFallback"]);function Et(t,e){return null!=t.treeNode&&mt(t.treeNode)?t.y-dt:t.y}function Mt(t,e,s){const i=t.treeNode,n={minX:t.x,minY:t.y,maxX:t.x+e,maxY:t.y+t.height};for(const t of i.children)yt(t,n,e);return D`
    <rect x="${n.minX-dt}" y="${n.minY-dt}" width="${n.maxX-n.minX+28}" height="${n.maxY-n.minY+28}"
          rx="${7}" fill="none"
          stroke="${s.arrow}" stroke-opacity="0.55"
          stroke-dasharray="10 5" stroke-width="1.5" />
  `}function Tt(t,e,s,i,n){const o=n?e.treeNode.info.stat:null;let r=0,a=0;if(n&&St.has(e.type)&&e.treeNode.children.length>0){const t=e.treeNode.children[0].info.stat;r=t?t.exchangesTotal:0,a=t?t.exchangesFailed:0}else r=o?o.exchangesTotal:0,a=o?o.exchangesFailed:0;const l=r-a,h=t.x+s/2,c=t.y+t.height,d=e.x+s/2,u=Et(e),p=n&&0===r?"10 5":"none";let f;if(h===d)f=`M ${h} ${c} L ${d} ${u-3}`;else{const t=c+20;f=`M ${h} ${c} L ${h} ${t} L ${d} ${t} L ${d} ${u-3}`}const $=`${d-6},${u-6} ${d},${u} ${d+6},${u-6}`;return D`
    <g class="arrow">
      <path d="${f}" fill="none" stroke="${i.arrow}" stroke-width="1.5"
            stroke-dasharray="${p}" />
      <polygon points="${$}" fill="${i.arrow}" />
      ${n&&l>0?D`
        <text x="${d+8}" y="${u-8}"
              fill="${i.counterOk}" font-size="10" font-family="sans-serif">${l}</text>
      `:""}
      ${n&&a>0?D`
        <text x="${d-8}" y="${u-8}" text-anchor="end"
              fill="${i.counterFail}" font-size="10" font-family="sans-serif">${a}</text>
      `:""}
    </g>
  `}function Ct(t,e,s,i){const n=t.treeNode.info.stat,o=n?n.exchangesTotal:0,r=n?n.exchangesFailed:0,a=o-r,l=t.x+e/2,h=Et(t),c=t.mergeCx,d=t.mergeY,u=i&&0===o?"10 5":"none";let p;if(c===l)p=`M ${c} ${d} L ${l} ${h-3}`;else{const t=d+(h-d)/2;p=`M ${c} ${d} L ${c} ${t} L ${l} ${t} L ${l} ${h-3}`}const f=`${l-6},${h-6} ${l},${h} ${l+6},${h-6}`;return D`
    <g class="arrow merge-arrow">
      <path d="${p}" fill="none" stroke="${s.arrow}" stroke-width="1.5"
            stroke-dasharray="${u}" />
      <polygon points="${f}" fill="${s.arrow}" />
      ${i&&a>0?D`
        <text x="${l+8}" y="${h-8}"
              fill="${s.counterOk}" font-size="10" font-family="sans-serif">${a}</text>
      `:""}
      ${i&&r>0?D`
        <text x="${l-8}" y="${h-8}" text-anchor="end"
              fill="${s.counterFail}" font-size="10" font-family="sans-serif">${r}</text>
      `:""}
    </g>
  `}function Pt(t,e,s,i,n={}){const o=function(t,e){if(null==t)return e.default;switch(t){case"from":return e.from;case"to":case"toD":case"wireTap":case"enrich":case"pollEnrich":return e.to;case"choice":case"when":case"otherwise":return e.choice;case"marshal":case"unmarshal":case"transform":case"setBody":case"setHeader":case"setProperty":case"convertBodyTo":case"removeHeader":case"removeHeaders":case"removeProperty":case"removeProperties":return e.transform;case"bean":case"process":case"log":case"script":case"delay":return e.processor;case"filter":case"split":case"aggregate":case"multicast":case"recipientList":case"routingSlip":case"dynamicRouter":case"loadBalance":case"circuitBreaker":case"saga":case"doTry":case"doCatch":case"doFinally":case"onException":case"onCompletion":case"intercept":case"loop":case"resequence":case"throttle":case"kamelet":case"pipeline":case"threads":return e.eip;default:return e.default}}(t.type,i),r=function(t,e=.2){const s=parseInt(t.slice(1,3),16),i=parseInt(t.slice(3,5),16),n=parseInt(t.slice(5,7),16),o=Math.min(255,Math.round(s+(255-s)*e)),r=Math.min(255,Math.round(i+(255-i)*e)),a=Math.min(255,Math.round(n+(255-n)*e));return`#${o.toString(16).padStart(2,"0")}${r.toString(16).padStart(2,"0")}${a.toString(16).padStart(2,"0")}`}(o),a=t.wrappedLines,l=n.selectable&&n.selectedNodeId===t.id,h=1.4*s,c=a.length*h,d=t.y+(t.height-c)/2+s,u=t.treeNode?t.treeNode.info.stat:null,p=n.nodeCounters&&u&&u.exchangesTotal>0;return D`
    <g class="node ${l?"node-selected":""}"
       data-node-id="${t.id}" data-node-type="${t.type}">
      <rect x="${t.x}" y="${t.y}" width="${e}" height="${t.height}"
            rx="${7}" fill="${o}"
            stroke="${l?i.selectedStroke:r}"
            stroke-width="${l?2.5:1}" />
      ${a.map((n,o)=>D`
        <text x="${t.x+e/2}" y="${d+o*h}"
              text-anchor="middle" fill="${i.text}"
              font-size="${s}" font-family="sans-serif">${n}</text>
      `)}
      ${p?D`
        <circle cx="${t.x+e-4}" cy="${t.y+4}"
                r="${9}" fill="${i.badgeBg}" />
        <text x="${t.x+e-4}" y="${t.y+4+3.5}"
              text-anchor="middle" fill="${i.badgeText}"
              font-size="9" font-weight="bold" font-family="sans-serif">${u.exchangesTotal}</text>
      `:""}
      ${n.selectable||n.tooltips?D`
        <rect class="node-hitarea" x="${t.x}" y="${t.y}"
              width="${e}" height="${t.height}"
              fill="transparent" style="cursor: pointer" />
      `:""}
    </g>
  `}function It(t,e,s,i,n,o={}){const r=[],a=[],l=[];let h=t.routeId;t.source&&(h+=` (${t.source})`);for(const s of t.nodes)null!=s.treeNode&&mt(s.treeNode)&&r.push(Mt(s,e,i));for(const s of t.nodes)null!=s.parentNode&&(s.connectFromMerge?a.push(Ct(s,e,i,n)):a.push(Tt(s.parentNode,s,e,i,n)));for(const n of t.nodes)l.push(Pt(n,e,s,i,o));const c=o.tooltips&&o.hoveredNodeId?t.nodes.find(t=>t.id===o.hoveredNodeId):null;return D`
    <g class="route" data-route-id="${t.routeId}">
      <text x="${ct}" y="${t.labelY+14}"
            fill="${i.label}" font-size="${s+1}"
            font-weight="bold" font-family="sans-serif">${h}</text>
      ${r}
      ${a}
      ${l}
      ${c?function(t,e,s,i){const n=t.treeNode?t.treeNode.info.stat:null,o=n?120:40,r=t.x+(e-180)/2,a=t.y-o-8;return D`
    <foreignObject x="${r}" y="${a}" width="${180}" height="${o}"
                   class="tooltip-fo" style="pointer-events: none; overflow: visible;">
      <div xmlns="http://www.w3.org/1999/xhtml"
           style="background: ${i.tooltipBg}; color: ${i.tooltipText};
                  border: 1px solid ${i.tooltipBorder}; border-radius: 6px;
                  padding: 6px 8px; font-size: 10px; font-family: sans-serif;
                  box-shadow: 0 2px 8px rgba(0,0,0,0.3);">
        <div style="font-weight: bold; margin-bottom: 4px;">
          ${t.type}${t.id?` (${t.id})`:""}
        </div>
        ${n?D`
          <table xmlns="http://www.w3.org/1999/xhtml"
                 style="width: 100%; border-collapse: collapse; font-size: 9px;">
            <tr><td>Total</td><td style="text-align:right">${n.exchangesTotal}</td>
                <td>Failed</td><td style="text-align:right; color:${i.counterFail}">${n.exchangesFailed}</td></tr>
            <tr><td>Inflight</td><td style="text-align:right">${n.exchangesInflight||0}</td>
                <td></td><td></td></tr>
            <tr><td>Mean</td><td style="text-align:right">${n.meanProcessingTime}ms</td>
                <td>Last</td><td style="text-align:right">${n.lastProcessingTime||"-"}ms</td></tr>
            <tr><td>Min</td><td style="text-align:right">${n.minProcessingTime}ms</td>
                <td>Max</td><td style="text-align:right">${n.maxProcessingTime}ms</td></tr>
          </table>
        `:""}
      </div>
    </foreignObject>
  `}(c,e,0,i):""}
    </g>
  `}class kt{constructor(t,e){this._host=t,this._fetchFn=e,this._intervalId=null}start(t){this.stop(),t>0&&(this._intervalId=setInterval(()=>this._fetchFn(),1e3*t))}stop(){null!=this._intervalId&&(clearInterval(this._intervalId),this._intervalId=null)}}class Ot extends lt{static properties={src:{type:String},refresh:{type:Number},filter:{type:String},nodeWidth:{type:Number,attribute:"node-width"},fontSize:{type:Number,attribute:"font-size"},nodeLabel:{type:String,attribute:"node-label"},metric:{type:Boolean},tooltips:{type:Boolean},nodeCounters:{type:Boolean,attribute:"node-counters"},selectable:{type:Boolean},panZoom:{type:Boolean,attribute:"pan-zoom"},fitView:{type:Boolean,attribute:"fit-view"},_routes:{state:!0},_error:{state:!0},_loading:{state:!0},_selectedNodeId:{state:!0},_hoveredNodeId:{state:!0},_viewTransform:{state:!0}};static styles=o`
    :host {
      display: block;
      font-family: sans-serif;
      --diagram-bg: transparent;
    }

    .container {
      overflow: auto;
      background: var(--diagram-bg);
    }

    .container.pan-zoom-active {
      overflow: hidden;
      cursor: grab;
    }

    .container.pan-zoom-active.panning {
      cursor: grabbing;
    }

    .loading {
      padding: 16px;
      color: var(--diagram-text, #888);
      font-style: italic;
    }

    .error {
      padding: 16px;
      color: var(--diagram-error, #e53935);
    }

    .empty {
      padding: 16px;
      color: var(--diagram-text, #888);
    }

    svg text {
      user-select: none;
    }

    .node {
      cursor: default;
    }

    .node:hover rect:first-child {
      filter: brightness(1.2);
    }

    .node-selected rect:first-child {
      filter: brightness(1.1);
    }
  `;constructor(){super(),this.src="",this.refresh=0,this.filter="",this.nodeWidth=180,this.fontSize=12,this.nodeLabel="code",this.metric=!0,this.tooltips=!1,this.nodeCounters=!1,this.selectable=!1,this.panZoom=!1,this.fitView=!1,this._routes=null,this._error=null,this._loading=!1,this._selectedNodeId=null,this._hoveredNodeId=null,this._viewTransform={x:0,y:0,scale:1},this._panning=!1,this._panStart=null,this._polling=new kt(this,()=>this._fetchData())}connectedCallback(){super.connectedCallback(),this.src&&this._fetchData(),this.refresh>0&&this._polling.start(this.refresh)}disconnectedCallback(){super.disconnectedCallback(),this._polling.stop()}updated(t){t.has("src")&&this.src&&!this._loading&&this._fetchData(),t.has("refresh")&&(this._polling.stop(),this.refresh>0&&this._polling.start(this.refresh)),t.has("filter")&&this.src&&!this._loading&&this._fetchData()}async _fetchData(){if(this.src){this._loading=!0;try{const t=await async function(t,e,s){const i=new URL(t,window.location.href);i.searchParams.set("metric",String(e)),s&&i.searchParams.set("filter",s);const n=await fetch(i,{headers:{Accept:"application/json"}});if(!n.ok)throw new Error(`Failed to fetch route structure: ${n.status} ${n.statusText}`);return n.json()}(this.src,this.metric,this.filter);this._routes=function(t){const e=[],s=t.routes;if(!s)return e;for(const t of s){const s={routeId:t.routeId,source:wt(t.source),nodes:[]},i=t.code;if(i)for(const t of i){const e={type:t.type,id:t.id,code:t.code,description:t.description,level:null!=t.level?t.level:0,stat:null};t.statistics&&(e.stat={exchangesTotal:t.statistics.exchangesTotal||0,exchangesFailed:t.statistics.exchangesFailed||0,exchangesInflight:t.statistics.exchangesInflight||0,meanProcessingTime:t.statistics.meanProcessingTime||0,maxProcessingTime:t.statistics.maxProcessingTime||0,minProcessingTime:t.statistics.minProcessingTime||0}),s.nodes.push(e)}e.push(s)}return e}(t),this._error=null}catch(t){this._error=t.message}finally{this._loading=!1}}}_getTheme(){return window.matchMedia&&window.matchMedia("(prefers-color-scheme: light)").matches?Nt:At}_findNodeById(t,e){for(const s of t){const t=s.nodes.find(t=>t.id===e);if(t)return t}return null}_handleSvgClick(t){const e=t.target.closest(".node");if(e&&this.selectable){const t=e.dataset.nodeId;this._selectedNodeId=t;const s=e.dataset.nodeType;this.dispatchEvent(new CustomEvent("node-selected",{detail:{nodeId:t,type:s},bubbles:!0,composed:!0}))}else this.selectable&&(this._selectedNodeId=null)}_handleNodeMouseEnter(t){if(!this.tooltips)return;const e=t.target.closest(".node");e&&(this._hoveredNodeId=e.dataset.nodeId)}_handleNodeMouseLeave(t){this.tooltips&&(this._hoveredNodeId=null)}_handleWheel(t){if(!this.panZoom)return;t.preventDefault();const e=t.deltaY>0?.9:1.1,s=this._viewTransform,i=Math.max(.2,Math.min(5,s.scale*e));this._viewTransform={...s,scale:i}}_handlePointerDown(t){if(!this.panZoom)return;this._panning=!0,this._panStart={x:t.clientX-this._viewTransform.x,y:t.clientY-this._viewTransform.y};const e=this.shadowRoot.querySelector(".container");e&&e.classList.add("panning"),t.target.setPointerCapture(t.pointerId)}_handlePointerMove(t){if(!this._panning||!this._panStart)return;const e=this._viewTransform;this._viewTransform={...e,x:t.clientX-this._panStart.x,y:t.clientY-this._panStart.y}}_handlePointerUp(t){if(!this._panning)return;this._panning=!1,this._panStart=null;const e=this.shadowRoot.querySelector(".container");e&&e.classList.remove("panning")}render(){if(this._error)return W`<div class="error">${this._error}</div>`;if(!this._routes)return W`<div class="loading">Loading route diagram...</div>`;if(0===this._routes.length)return W`<div class="empty">No routes found.</div>`;const t=this._getTheme(),e={nodeWidth:this.nodeWidth,fontSize:this.fontSize,nodeLabel:this.nodeLabel},s=[];let i=ct;for(const t of this._routes){const n=bt(t,i,e);s.push(n),i=n.maxY+40}const n=i,o={tooltips:this.tooltips,hoveredNodeId:this._hoveredNodeId,nodeCounters:this.nodeCounters,selectable:this.selectable,selectedNodeId:this._selectedNodeId,panZoom:this.panZoom,viewTransform:this._viewTransform,fitView:this.fitView},r=function(t,e,s,i,n,o,r={}){const a=Math.max(...t.map(t=>t.maxX),400)+ct,l=e+ct;let h;if(r.fitView){const t=.05*Math.max(a,l);h=`${-t} ${-t} ${a+2*t} ${l+2*t}`}else h=`0 0 ${a} ${l}`;const c=t.map(t=>It(t,s,i,n,o,r));if(r.panZoom){const t=r.viewTransform||{x:0,y:0,scale:1};return D`
      <svg xmlns="http://www.w3.org/2000/svg"
           viewBox="${h}"
           width="100%" height="100%"
           style="max-width: ${r.fitView?"100%":a+"px"}">
        <g class="pan-zoom-layer"
           transform="translate(${t.x}, ${t.y}) scale(${t.scale})">
          ${c}
        </g>
      </svg>
    `}return D`
    <svg xmlns="http://www.w3.org/2000/svg"
         viewBox="${h}"
         width="100%" height="100%"
         style="max-width: ${r.fitView?"100%":a+"px"}">
      ${c}
    </svg>
  `}(s,n,this.nodeWidth,this.fontSize,t,this.metric,o),a="container"+(this.panZoom?" pan-zoom-active":"");return W`
      <div class="${a}"
           @click="${this._handleSvgClick}"
           @mouseover="${this._handleNodeMouseEnter}"
           @mouseout="${this._handleNodeMouseLeave}"
           @wheel="${this._handleWheel}"
           @pointerdown="${this._handlePointerDown}"
           @pointermove="${this._handlePointerMove}"
           @pointerup="${this._handlePointerUp}">
        ${r}
      </div>
    `}}customElements.define("camel-route-diagram",Ot);export{Ot as CamelRouteDiagram};
