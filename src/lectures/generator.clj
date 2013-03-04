(ns lectures.generator
  (:require [clojure.string :as str])
  (:use hiccup.core)
  (:import java.text.SimpleDateFormat
           java.util.Locale))

(defn- generate-dispatch [ast]
  (cond (string? ast) :text
        (keyword? (first ast)) (first ast)
        :else :text-line))

(defmulti generate
  #'generate-dispatch)

(defmethod generate :text [string]
  string)

(defmethod generate :code [[_ code]]
  [:code (h code)])

(defmethod generate :bold [[_ text]]
  [:strong text])

(defmethod generate :text-line [chunks]
  (mapv generate chunks))

(defmethod generate :paragraph [[_ & chunks]]
  `[:p ~@(generate chunks)])

(defmethod generate :block [[_ kind code]]
  [:pre code])

(defmethod generate :bullet-list [[_ & items]]
  `[:ul ~@(map generate items)])

(defmethod generate :incremental [[_ & chunks]]
  `[:li {:class "action"} ~@(generate chunks)])

(defmethod generate :static [[_ & chunks]]
  `[:li ~@(generate chunks)])

(defmethod generate :slide [[_ title subtitle & chunks]]
  `[:section {:class "slide"}
    [:hgroup [:h1 ~@(generate title)]
             ~@(when subtitle `[[:h2 ~@(generate subtitle)]])]
    ~@(map generate chunks)])

(defmethod generate :presentation [[_ & slides]]
  (mapv generate slides))

(defn- format-date
  [date]
  (-> (SimpleDateFormat. "dd MMMM yyyy" (Locale. "bg"))
      (.format date)
      str/lower-case))

(defn generate-lecture
  [title date ast]
  (html
    `[:html {:lang "bg"}
      [:head
       [:meta {:charset "utf-8"}]
       [:link {:rel "stylesheet" :href "css/styles.css"}]
       [:title ~title]]
      [:body
       [:header
        [:h1 ~title]
        [:nav
         [:ul
          [:li [:button {:id "prev-btn" :title "Previous slide"} "Previous slide"]]
          [:li [:span {:id "slide-number"}] "/" [:span {:id "slide-total"}]]
          [:li [:button {:id "next-btn" :title "Next slide"} "Next slide"]]]]]
       [:div {:id "deck"}
        [:section
         [:hgroup
          [:h1 ~title]
          [:h2 ~(format-date date)]]]
        ~@(generate ast)
        [:section
         [:hgroup [:h1 "Въпроси"]]
         [:ul
          [:li [:a {:href "http://fmi.clojure.bg/topics"} "http://fmi.clojure.bg"]]
          [:li [:a {:href "http://clojure.github.com/clojure/"} "Официална документация"]]
          [:li [:a {:href "http://twitter.com/clojurefmi"} "@clojurefmi"]]]]]
       [:script {:src "js/jquery-1.5.2.min.js"}]
       [:script {:src "js/jquery.jswipe-0.1.2.min.js"}]
       [:script {:src "js/htmlSlides.js"}]
       [:script {:type "text/javascript"} "$(function() { htmlSlides.init({hideToolbar: true}); });"] ]]))
