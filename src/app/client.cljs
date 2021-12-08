(ns app.client
  (:require
   [com.fulcrologic.fulcro.application :as app]
   [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
   [com.fulcrologic.fulcro.dom :as dom]
   [com.fulcrologic.fulcro.algorithms.merge :as merge]
   [com.fulcrologic.fulcro.algorithms.data-targeting :as targeting]))

(defsc Car [this {:car/keys [id model] :as props}]
  {:query [:car/id :car/model]
   :ident :car/id}
  (dom/div
   "Model " model))

(def ui-car (comp/factory Car {:keyfn :car/id}))

(defsc Person [this {:person/keys [id name age cars] :as props}]
  {:query [:person/id :person/name :person/age {:person/cars (comp/get-query Car)}]
   :ident :person/id}
  (dom/div
   (dom/div "Name: " name)
   (dom/div "Age: " age)
   (dom/h3 "Cars")
   (dom/ul
    (map ui-car cars))))

(def ui-person (comp/factory Person {:keyfn :person/id}))

(defsc Sample [this {:root/keys [person]}]
  {:query [{:root/person (comp/get-query Person)}]}
  (dom/div
   (ui-person person)))

(defonce APP (app/fulcro-app))

(defn ^:export init []
  (app/mount! APP Sample "app"))

(comment

  ;; Examples of playing around with data, and with normalized data.
  ;; We are performing surgery on the Fulcro DB.
  ;; Not how you actually write a Fulcro app.

  (keys APP)

  (reset! (::app/state-atom APP) {})

  @(::app/state-atom APP)

  (reset! (::app/state-atom APP) {:sample
                                  {:person/name "Joe"
                                   :person/cars [{:car/id    22
                                                  :car/model "Escort"}
                                                 {:car/id    23
                                                  :car/model "Focus"}]}})

  @(::app/state-atom APP)

  (merge/merge-component! APP Person {:person/id   2
                                      :person/name "Bob"
                                      :person/age  21
                                      :person/cars [{:car/id    1
                                                     :car/model "F-150"}]}
                          :replace [:root/person])

  @(::app/state-atom APP)

  (merge/merge-component! APP Car {:car/id    3
                                   :car/model "Cortina"}
                          :append [:person/id 2 :person/cars])

  @(::app/state-atom APP)

  (app/schedule-render! APP)

  (comp/get-query Person)

  (comp/get-ident Car {:car/id 100 :anything 1000})

  (meta (comp/get-query Person))

  ;; It's just simpleClojure data:
  (do (swap! (::app/state-atom APP) update-in [:person/id 2 :person/age] inc)
      (app/schedule-render! APP))
  )
