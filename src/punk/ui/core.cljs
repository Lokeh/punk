(ns punk.ui.core
  (:require [hx.react :as hx :refer [defnc]]
            [hx.utils]
            [hx.react.hooks :refer [<-state <-effect]]
            ["react-is" :as react-is]
            ["react-dom" :as react-dom]
            [goog.object :as gobj]
            [clojure.string :as s]
            [clojure.datafy :as d]
            [clojure.core.protocols :as p]))

;;
;; App panes
;;

(defnc View [{:keys [data on-next] :as props}]
  (when (not (nil? data))
    [:div (dissoc props :on-next :data)
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
       [:div {:on-click #(on-next data nil nil)}
        (prn-str data)])]))

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
                 (swap! state update :log conj (dataficate x)))]
    ;; add tap listener
    (<-effect (fn []
                #_(dbg> "Adding tap")
                (add-tap tap-fn)
                (fn []
                  #_(dbg> "removing tap")
                  (remove-tap tap-fn)))
              #_[state])

    [:div {:style {:display "flex"
                   :height "100%"
                   :flex-direction "column"}}
     ;; css
     [Style
      "#current { overflow: scroll }"
      "#current .item { cursor: pointer; padding: 3px; margin: 3px; }"
      "#current .item:hover { background-color: #eee; }"

      "#next { overflow: scroll }"
      "#next { cursor: pointer; padding: 3px; margin: 3px; }"
      "#next:hover { background-color: #eee; }"

      "#log { overflow: scroll }"
      "#log .item { cursor: pointer; padding: 3px 0; margin: 3px 0; }"
      "#log .item:hover { background-color: #eee; }"]
     ;; Next
     [:h3 "Next"]
     [:div {:style {:flex 1
                    :position "relative"
                    :display "flex"
                    :flex-direction "column"}}
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
     [:h3 "Current"]
     [:div {:style {:flex 1
                    :position "relative"
                    :display "flex"
                    :flex-direction "column"}}
      [View {:data (d/datafy (:current @state))
             :id "current"
             :on-next #(swap! state assoc
                              :next
                              {:coll %1
                               :k %2
                               :v %3})}]]
     ;; Controls
     [:div
      [:button {:type "button"
                :style {:width "60px"}
                :disabled (empty? (:history @state))
                :on-click #(swap! state assoc
                                  :current (peek (:history @state))
                                  :history (pop (:history @state))
                                  :next {:coll nil :k nil :v nil})}"<"]]

     ;; Log
     [:h3 "Log"]
     [:div {:style {:flex 1
                    :position "relative"
                    :display "flex"
                    :flex-direction "column"}
            :id "log"}
      (for [datum (:log @state)]
        [:div {:on-click #(swap! state assoc
                                 :current datum
                                 :history [])
               :class "item"}
         (prn-str (d/datafy datum))])]]))

#_(tap> #js {:asdf "jkl"})
#_(tap> (js/Date.))
#_(tap> (js/RegExp.))

(defn start! []
  (let [container (or (. js/document getElementById "punk")
                      (let [new-container (. js/document createElement "div")]
                        (dbg> "Creating new container")
                        (. new-container setAttribute "id" "punk")
                        (-> js/document .-body (.appendChild new-container))
                        new-container))]
    (dbg> "starting")
    (react-dom/render (hx/f [App]) container)))