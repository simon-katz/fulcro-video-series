(ns app.client
  (:require
   ["react-number-format" :as NumberFormat]
   [com.fulcrologic.fulcro.application :as app]
   [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
   [com.fulcrologic.fulcro.dom :as dom :refer [div ul li h3 label button]]
   [com.fulcrologic.fulcro.algorithms.merge :as merge]
   [com.fulcrologic.fulcro.algorithms.react-interop :as interop]
   [com.fulcrologic.fulcro.mutations :as m :refer [defmutation]]
   [com.fulcrologic.fulcro.routing.dynamic-routing :as dr]
   [com.fulcrologic.fulcro.algorithms.data-targeting :as targeting]))

(def ui-number-format (interop/react-factory NumberFormat))

(defsc Car [this {:car/keys [id model] :as props}]
  {:query         [:car/id :car/model]
   :ident         :car/id
   :initial-state {:car/id    :param/id
                   :car/model :param/model}}
  (div
   "Model " model))

(def ui-car (comp/factory Car {:keyfn :car/id}))

(defmutation make-older [{:person/keys [id]}]
  (action [{:keys [state]}]
          (swap! state update-in [:person/id id :person/age] inc)))

(defsc Person [this {:person/keys [id name age cars] :as props}]
  {:query         [:person/id :person/name :person/age {:person/cars (comp/get-query Car)}]
   :ident         :person/id ; shorthand when table name and id key are the same: (fn [] [:person/id (:person/id props)])
   :initial-state {:person/id   :param/id
                   :person/name :param/name
                   :person/age  20
                   :person/cars [{:id 40 :model "Leaf"}
                                 {:id 41 :model "Escort"}
                                 {:id 42 :model "Sienna"}]}
   ;; Can use `:initLocalState` to store useful things, to save on repeatedly
   ;; doing stuff (like creating lambdas) when rendering.
   :initLocalState (fn [_this _props]
                     {:onClick (fn [_] (js/console.log "Click on name label"))})}
  (let [onClick (comp/get-state this :onClick)]
    (div :.ui.segment
         (div :.ui.form
              (div :.field
                   (label {:onClick onClick} "Name: ")
                   name)
              (div :.field
                   (label "Amount: ")
                   (ui-number-format {:value "1100221.33"
                                      :thousandSeparator true
                                      :prefix            "$"}))
              (div :.field
                   (label "Age: ")
                   age)
              (button {:onClick #(comp/transact! this [(make-older {:person/id id})])} "Make older")
              (h3 {} "Cars")
              (ul {}
                  (map ui-car cars))))))

(def ui-person (comp/factory Person {:keyfn :person/id}))

(defsc PersonList [this {:person-list/keys [people] :as props}]
  {:query         [{:person-list/people (comp/get-query Person)}]
   :ident         (fn [_ _] [:component/id ::person-list]) ; this should be a fn of no args (but ClojureScript/JavaScript isn't fussy)
   :initial-state {:person-list/people [{:id 1 :name "Bob"}
                                        {:id 2 :name "Sally"}]}}
  (div
   (h3 "People")
   (map ui-person people)))

(def ui-person-list (comp/factory PersonList))

(defsc Sample [this {:root/keys [people]}]
  {:query         [{:root/people (comp/get-query PersonList)}]
   :initial-state {:root/people {}}}
  (div
   (when people
     (ui-person-list people))))

(defonce APP (app/fulcro-app))

(defn ^:export init []
  ;; Needed for Fulcro Inspect to show the DB after browser refresh:
  (app/set-root! APP Sample {:initialize-state? true})
  (dr/initialize! APP)
  ;; Other init
  (app/mount! APP Sample "app" {:initialize-state? false}))

(comment
  (comp/component-options Person)

  (comp/transact! APP [(make-older {:person/id 1})])

  (app/current-state APP)

  (merge/merge-component! APP Person {:person/id 1 :person/age 20})

  (comp/get-initial-state Car {:id 78 :model "Cortina"})
  (comp/get-initial-state Person {:id 1 :name "Bob"})
  (comp/get-initial-state Sample)

  ;; Calling a mutation function returns data that represents the
  ;; desired mutation. See your notes.
  (make-older {:person/id 1})
  ;; => (app.client/make-older #:person{:id 1})
  )
