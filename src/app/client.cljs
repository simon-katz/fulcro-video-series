(ns app.client
  (:require
   [app.model.person :refer [make-older picker-path]]
   [com.fulcrologic.fulcro.application :as app]
   [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
   [com.fulcrologic.fulcro.dom :as dom]
   [com.fulcrologic.fulcro.networking.http-remote :as http]
   [com.fulcrologic.fulcro.mutations :as m :refer [defmutation]]
   [com.fulcrologic.fulcro.data-fetch :as df]))

;;;; ___________________________________________________________________________

(defsc Car [this {:car/keys [id model] :as props}]
  {:query [:car/id :car/model]
   :ident :car/id}
  (dom/div
      "Model " model))

(def ui-car (comp/factory Car {:keyfn :car/id}))

;;;; ___________________________________________________________________________

(defsc PersonDetail [this {:person/keys [id name age cars] :as props}]
  {:query [:person/id
           :person/name
           :person/age
           {:person/cars (comp/get-query Car)}]
   :ident :person/id}
  (let [onClick (comp/get-state this :onClick)]
    (dom/div :.ui.segment
      (dom/h3 :.ui.header "Selected Person")
      (when id
        (dom/div :.ui.form
          (dom/div :.field
            (dom/label {:onClick onClick} "Name: ")
            name)
          (dom/div :.field
            (dom/label "Age: ")
            age)
          (dom/button :.ui.button
            {:onClick (fn []
                        (comp/transact! this
                                        [(make-older {:person/id id})]
                                        {:refresh [:person-list/people]}))}
            "Make Older")
          (dom/h3 {} "Cars")
          (dom/ul {}
            (map ui-car cars)))))))

(def ui-person-detail (comp/factory PersonDetail {:keyfn :person/id}))

;;;; ___________________________________________________________________________

(defsc PersonListItem [this {:person/keys [id name]}]
  {:query [:person/id :person/name]
   :ident :person/id}
  (dom/li :.item
    (dom/a
        {:href    "#"
         :onClick (fn []
                    (df/load! this [:person/id id] PersonDetail
                              {:target (picker-path
                                        :person-picker/selected-person)}))}
      name)))

(def ui-person-list-item (comp/factory PersonListItem {:keyfn :person/id}))

;;;; ___________________________________________________________________________

(defsc PersonList [this {:person-list/keys [people]}]
  {:query         [{:person-list/people (comp/get-query PersonListItem)}]
   :ident         (fn [] [:component/id :person-list])
   :initial-state {:person-list/people []}}
  (dom/div :.ui.segment
    (dom/h3 :.ui.header "People")
    (dom/ul
        (map ui-person-list-item people))))

(def ui-person-list (comp/factory PersonList))

;;;; ___________________________________________________________________________

(defsc PersonPicker [this {:person-picker/keys [list selected-person]}]
  {:query         [{:person-picker/list (comp/get-query PersonList)}
                   {:person-picker/selected-person (comp/get-query
                                                    PersonDetail)}]
   :initial-state {:person-picker/list {}}
   :ident         (fn [] [:component/id :person-picker])}
  (dom/div :.ui.two.column.container.grid
    (dom/div :.column
      (ui-person-list list))
    (dom/div :.column
      (if selected-person
        (ui-person-detail selected-person)
        (dom/div :.ui.segment "No selection")))))

(def ui-person-picker (comp/factory PersonPicker
                                    {:keyfn :person-picker/people}))

;;;; ___________________________________________________________________________

(defsc Root [this {:root/keys [person-picker]}]
  {:query         [{:root/person-picker (comp/get-query PersonPicker)}]
   :initial-state {:root/person-picker {}}}
  (dom/div :.ui.container.segment
    (dom/h3 "Application")
    (ui-person-picker person-picker)))

;;;; ___________________________________________________________________________

(defonce APP
  (app/fulcro-app
   {:remotes          {:remote (http/fulcro-http-remote {})}
    :client-did-mount (fn [app]
                        (df/load! app :all-people PersonListItem
                                  {:target [:component/id
                                            :person-list
                                            :person-list/people]}))}))

;;;; ___________________________________________________________________________

(defn ^:export init []
  (app/mount! APP Root "app"))

;;;; ___________________________________________________________________________

(comment
  (df/load! APP [:person/id 1] PersonDetail))
