(ns punk.workshop.devcards
  (:require [devcards.core :as dc :include-macros true]
            [punk.ui.components :as punk-ui]
            [hx.react :as hx]))

(devcards.core/start-devcard-ui!)

(dc/defcard pane
  (hx/f [:div {:style {:position "relative"
                       :height "200px"} }
         [punk-ui/Pane {:title "Title" :id "pane-1"}
          [:div "Children"]]]))

(dc/defcard pane-with-long-content
  (hx/f [:div {:style {:height "200px"}}
         [punk-ui/Pane {:title "Title" :id "pane-1"}
          (for [n (range 20)]
            [:div "Child " n])]]))

(hx/defnc Controls [_]
  [:button " < "])

(dc/defcard pane-with-controls
  (hx/f [:div {:style {:height "200px"}}
         [punk-ui/Pane {:title "Title" :id "pane-1"
                        :controls [Controls]}
          (for [n (range 20)]
            [:div "Child " n])]]))

(dc/defcard table-coll
  (hx/f [punk-ui/Table {:cols [[:idx first {:flex 1}]
                               [:value second {:flex 3}]]
                        :data (map-indexed vector [1 2 3 4])}]))

(dc/defcard table-map
  (hx/f [punk-ui/Table {:cols [[:key (comp str first) {:flex 1}]
                               [:value (comp prn-str second) {:flex 3}]]
                        :data {:foo "bar" :baz #{1 2 3}}}]))

(dc/defcard table-multiple-things
  (hx/f [punk-ui/Table {:cols [[:key (comp str first) {:flex 1}]
                               [:value (comp prn-str second) {:flex 3}]
                               [:meta (comp prn-str meta second) {:flex 3}]]
                        :data {:foo (with-meta {:asdf "jkl"} {:punk/tag 'cljs.core/Map})
                               :baz (with-meta #{1 2 3} {:punk/tag 'cljs.core/Set})}}]))
