(ns parens.pages
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [hiccup.core :refer [html]]
            [parens.episodes :refer [episode-url episode-name embed-video]]
            [stasis.core :as stasis]))

(defn no-widows
  "Avoid typographic widows by adding a non-breaking space between the last two words."
  [s]
  (str/replace s #" ([^ ]{1,6})$" "&nbsp;$1"))

(defn- render-episode [episode]
  [:a {:class (str "box episode-box " (:color episode))
       :href (episode-url episode)}
   [:h2 (no-widows (episode-name episode))]
   (:description episode)])

(defn index [content]
  {:body
   (list
    [:div.illustration]
    [:div.content
     (map render-episode (:episodes (first (:seasons content))))
     [:p.next
      "Next episode? Follow "
      [:a {:href "https://twitter.com/parensofthedead"}
       "@parensofthedead"]]])
   :color "red"})

(defn- insert-disqus-thread [html episode]
  (-> html
      (str/replace #":episode-identifier" (str "episode_" (-> episode :prefixes :disqus) (:number episode)))
      (str/replace #":episode-link" (episode-url episode))))

(defn- episode-page [episode next-episode content]
  {:body
   (list
    [:div.episode
     [:div.content
      [:div.video-embed (embed-video episode)]
      [:div.box
       [:h2 (no-widows (:name episode))]
       (:description episode)]
      [:div.box
       [:h3 "Done watching?"]
       [:ul
        (if next-episode
          [:li "Check out " [:a {:href (episode-url next-episode)} (episode-name next-episode)] "."]
          [:li "Follow " [:a {:href "https://twitter.com/parensofthedead"}
                          "@parensofthedead"] " to be notified when the next episode is ready."])
        [:li "Take a look at the " [:a {:href "/"} "episode overview"] "."]
        [:li "Leave your comments or questions below. Underground."]]]]]
    [:div.comments.content
     [:div.box
      (insert-disqus-thread (:disqus-html content) episode)]])
   :color (:color episode)})

(defn create-episode-pages [content]
  (->> (:seasons content)
       (mapcat :episodes)
       (partition-all 2 1)
       (map (fn [[episode next-episode]]
              [(episode-url episode)
               (episode-page episode next-episode content)]))
       (into {})))

(defn get-pages [content]
  (merge
   {"/" (index content)}
   (create-episode-pages content)))
