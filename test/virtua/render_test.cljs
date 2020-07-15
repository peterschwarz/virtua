(ns virtua.render-test
  (:require [cljs.test :refer-macros [deftest testing async is are use-fixtures]]
            [virtua.dom :refer [child-at child-count]]
            [virtua.render :as r]
            [virtua.test-utils
             :refer [css-class tag text-content]
             :refer-macros [with-container]]))

(deftest test-render-single-element
  (testing "rendering a simple element"
    (with-container el
      (r/render el [nil '([:div nil 1] "hello") nil])

      (is (= 1 (child-count el)))
      (is (= "div" (tag (child-at el 0))))
      (is (= "hello" (-> (child-at el 0)
                         text-content))))))


(deftest test-render-nested-elements
  (testing "rendering nested elements"
    (with-container el
      (r/render
        el
        [nil
         '([:div nil 2]
          [:p nil 1]
          "paragraph1"
          [:p nil 1]
          "paragraph2")
        nil])

      (is (= 1 (child-count el)))
      (let [div (child-at el 0)]
        (is (= "div" (tag div)))
        (is (= 2 (child-count div)))

        (is (= "paragraph1"
               (-> (child-at div 0)
                   text-content)))
        (is (= "paragraph2"
               (-> (child-at div 1)
                   text-content)))))))

(deftest test-render-with-tree-diff
  (testing "rendering with simple text change"
    (with-container el
      ; render first with initial tree
      (r/render el [nil '([:div nil 1] "Test Text") nil])
      (is (= 1 (child-count el)))
      (is (= "div" (tag (child-at el 0))))
      (is (= "Test Text"
             (-> (child-at el 0)
                 text-content)))

      ; render with the text changed
      (r/render el [[nil "Test Text"] [nil "Changed Text"] [[:div nil 1]]])

      (is (= "Changed Text"
             (-> (child-at el 0)
                 text-content))))))

(deftest test-render-with-diff-additive
  (testing "rendering with an additive difference"
    (with-container el
      (r/render el [nil '([:ul nil 2] [:li nil 1] "always shown") nil])

      (is (= 1 (child-count el)))
      (let [ul (child-at el 0)]
        (is (= 1 (child-count ul)))
        (is (= "always shown"
               (-> (child-at ul 0)
                   text-content)))

        (r/render
          el
          [nil ; old
           [nil nil nil [:li nil 1] "Now showing: additive"] ; new
           [[:ul nil 2] [:li nil 1] "always shown"]]) ; same

        (is (= 2 (child-count ul)))
        (is (= "always shown"
               (-> (child-at ul 0)
                   text-content)))
        (is (= "Now showing: additive"
               (-> (child-at ul 1)
                   text-content)))))))

(deftest test-render-with-diff-reductive
  (testing "rendering with a reductive difference"
    (with-container el
      (r/render
        el
        [nil
         '([:ul nil 2]  [:li nil 1] "always shown"  [:li nil 1] "Now showing: reductive")
         nil])
      (is (= 1 (child-count el)))
      (let [ul (child-at el 0)]
        (is (= 2 (child-count ul)))
        (is (= "always shown"
               (-> (child-at ul 0)
                   text-content)))
        (is (= "Now showing: reductive"
               (-> (child-at ul 1)
                   text-content)))

        (r/render
         el
         [[[nil nil 2] nil nil [:li nil 1] "Now showing: reductive"]
         [[nil nil 1]]
         [[:ul nil] [:li nil 1] "always shown"]])

        (is (= 1 (child-count ul)))
        (is (= "always shown"
               (-> (child-at ul 0)
                   text-content)))
        (is (nil? (child-at ul 1)))))))

(deftest test-render-with-diff-insertive
  (testing "rendering with an insertive difference"
    (with-container el
      (r/render
        el
        [nil '([:ul nil 1] [:li nil 1] "always shown") nil])

      (is (= 1 (child-count el)))
      (let [ul (child-at el 0)]
        (is (= 1 (child-count ul)))
        (is (= "always shown"
               (-> (child-at ul 0)
                   text-content)))

        (r/render
          el
          [[[nil nil 1] nil "always shown"]
           [[nil nil 2] nil "Now showing: insertive" [:li nil 1] "always shown"]
           [[:ul nil] [:li nil 1]]])

        (is (= 2 (child-count ul)))
        (is (= "Now showing: insertive"
               (-> (child-at ul 0)
                   text-content)))
        (is (= "always shown"
               (-> (child-at ul 1)
                   text-content)))))))

(deftest test-render-with-diff-attribute-change
  (testing "rendering with an attribute difference"
    (with-container el
      (r/render
        el
        [nil
         '([:p  {:class "text-success"} 1] "Text")])

      (is (= 1 (child-count el)))
      (let [p (child-at el 0)]
        (is (= "text-success" (css-class p)))
        (is (= "Text" (text-content p)))

        (r/render
          el
          [[[nil {:class "text-success"}]]
           [[nil {:class "text-danger"}]]
           [[:p nil 1] "Text"]])

        (is (= "text-danger" (css-class p)))
        (is (= "Text" (text-content p)))))))

(deftest test-render-with-pseudo-tag-seq
  (testing "render with an pseudo-tag for sequences"
    (with-container el
      (r/render
        el
        [nil
         '([:ul nil 1]
           [:virtua/seq nil 3]
           [:li nil 1] 1
           [:li nil 1] 2
           [:li nil 1] 3)])

      (is (= 1 (child-count el)))
      (let [ul (child-at el 0)]
        (is (= "ul" (tag ul)))
        (is (= 3 (child-count ul)))

        (doseq [i (range 1 4)]
          (let [li (child-at ul (dec i))]
            (is (= "li" (tag li)))
            (is (= (str i) (text-content li))))))))

  (testing "render with an pseudo-tag difference"
    (with-container el
      (r/render
        el
        [nil
        '([:ul nil 1] [:virtua/seq nil 0])])

      (is (= 1 (child-count el)))
      (let [ul (child-at el 0)]
        (is (= "ul" (tag ul)))
        (is (= 0 (child-count ul)))

        (r/render
          el
          [[nil [nil nil 0]]
           [nil [nil nil 3] [:li nil 1] 1 [:li nil 1] 2 [:li nil 1] 3]
           [[:ul nil 1] [:virtua/seq nil]]])

        (is (= 3 (child-count ul)))

        (doseq [i (range 1 4)]
          (let [li (child-at ul (dec i))]
            (is (= "li" (tag li)))
            (is (= (str i) (text-content li)))))))))

(deftest test-render-emit
  (testing "render with an add emit"
    (with-container el
      (let [events (atom [])]
        (r/render
          el
          #(swap! events conj %)
          [nil
           '([:h1 nil 1] "Text")])

        (let [[event el-decl el] (first @events)]
          (is (= :virtua/add event))
          (is (= [:h1 nil 1] el-decl))
          (is (= "h1" (tag el)))))))

  (testing "render with remove emit"
    (with-container el
      (let [events (atom [])]
        (r/render
          el
          [nil
           '([:h1 nil 1] "Text")])
        ; remove it
        (r/render
          el
          #(swap! events conj %)
          [[[:h1 nil 1] "Text"]
           [[:div nil 1] "More text"]
           nil])

        (let [[event el-decl el] (first @events)]
          (is (= :virtua/remove event))
          (is (= [:h1 nil 1] el-decl))
          (is (= "h1" (tag el))))

        (let [[event el-decl] (get @events 1)]
          (is (= :virtua/add event))
          (is (= [:div nil 1] el-decl))
          (is (= "div" (tag el))))))))
