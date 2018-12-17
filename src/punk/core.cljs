(ns punk.core
  (:require [hx.react :as hx :refer [defnc]]
            [hx.react.hooks :refer [<-state <-effect]]
            ["react-dom" :as react-dom]
            [clojure.string :as s]
            [clojure.datafy :as d]))

(def dbg> (partial js/console.log "punk>"))

(extend-protocol IPrintWithWriter
  js/Symbol
  (-pr-writer [sym writer _]
    (-write writer (str "\"" (.toString sym) "\""))))


;;
;; Data structures
;;

(defprotocol WithIndex
  (with-index [this]))


(extend-protocol WithIndex
  cljs.core/PersistentVector
  (with-index [v] (map-indexed vector v))

  cljs.core/PersistentHashSet
  (with-index [s] (map-indexed vector s))

  default
  (with-index [x] x))

;;
;; App panes
;;

(defnc View [{:keys [data on-next] :as props}]
  [:div props
   (if (coll? data)
     [:<>
      [:div {:style {:display "flex"
                     :border-bottom "1px solid #999"
                     :padding-bottom "3px"
                     :margin-bottom "3px"}}
       [:div {:style {:flex 1}} "key"]
       [:div {:style {:flex 2}} "value"]]
      (for [[key v] (with-index data)]
        [:div {:style {:display "flex"}
               :key key
               :class "item"
               :on-click #(on-next data key v)}
         [:div {:style {:flex 1}}
          (prn-str key)]
         [:div {:style {:flex 2}}
          (prn-str v)]])]
     (when (not (nil? data))
       [:div {:on-click #(on-next data nil nil)}
        (prn-str data)]))])

(defnc Style [{:keys [children]}]
  [:style {:dangerouslySetInnerHTML #js {:__html (s/join "\n" children)}}])

(defnc App [_]
  (let [state (<-state {:log [{:foo ["bar" "baz"]
                               :bar {:baz 42}}]
                        :history []
                        :current {:foo ["bar" "baz"]
                                  :bar {:baz 42}}
                        :next {:coll nil
                               :k nil
                               :v nil}})
        tap-fn (fn [x]
                 (dbg> @state)
                 (swap! state assoc :log (conj (:log @state) x)))]
    (<-effect (fn []
                (dbg> "Adding watch")
                (add-tap tap-fn)
                (fn []
                  (dbg> "removing watch")
                  (remove-tap tap-fn)))
              [state])
    #_(dbg> @state)
    [:div {:style {:display "flex"
                   :height "100%"
                   :flex-direction "column"}}
     ;; css
     [Style
      "#current .item { cursor: pointer; padding: 3px; margin 3px; }"
      "#current .item:hover { background-color: #eee; }"
      "#next { cursor: pointer; padding: 3px; margin 3px; }"
      "#next:hover { background-color: #eee; }"
      "#log .item { cursor: pointer; padding: 3px 0; margin: 3px 0; }"
      "#log .item:hover { background-color: #eee; }"]
     ;; Next
     [:div {:style {:flex 1}}
      [:h3 "Next"]
      [View {:data (d/datafy
                    (d/nav (-> @state :next :coll)
                           (-> @state :next :k)
                           (-> @state :next :v)))
             :id "next"
             :on-next #(swap! state
                              assoc
                              :history (conj (:history @state)
                                             (:current @state))
                              :current %1
                              :next {:coll nil
                                     :k nil
                                     :v nil})}]]
     ;; Current
     [:div {:style {:flex 1}}
      [:h3 "Current"]
      [View {:data (d/datafy (:current @state))
             :id "current"
             :on-next #(swap! state assoc
                              :next
                              {:coll %1
                               :k %2
                               :v %3})}]]
     [:div [:button {:type "button"
                     :disabled (empty? (:history @state))
                     :on-click #(swap! state assoc
                                       :current (peek (:history @state))
                                       :history (pop (:history @state))
                                       :next {:coll nil :k nil :v nil})}"<"]]
     ;; Log
     [:div {:style {:flex 1}
            :id "log"}
      [:h3 "Log"]
      (for [datum (:log @state)]
        [:div {:on-click #(swap! state assoc
                                 :current datum
                                 :history [])
               :class "item"}
         (prn-str datum)])]]))


(defn start! []
  (let [container (or (. js/document getElementById "punk")
                      (let [new-container (. js/document createElement "div")]
                        (dbg> "Creating new container")
                        (. new-container setAttribute "id" "punk")
                        (-> js/document .-body (.appendChild new-container))
                        new-container))]
    (dbg> "starting")
    (react-dom/render (hx/f [App]) container)))

