(ns leave-balance-service.date-utils
  (:gen-class)
  (:require [java-time :as time]))

(defn date-now
  "The current date."
  []
  (time/local-date))

(defn date->str
  "Convert a date object to a string in 'YYYY-MM-dd' format."
  [date-obj]
  (time/format "yyyy-MM-dd" date-obj))

(defn str->date
  "Convert a date in string format 'YYYY-MM-dd' to a date object."
  [date-string]
  (time/local-date "yyyy-MM-dd" date-string))

(defn date-now-string
  "Return a string format of the current date."
  []
  (date->str (date-now)))

(defn cur-year
  "The current year from the current date."
  []
  (time/as (date-now) :year))

(defn end-of-year
  "Last date of the current year."
  []
  (time/local-date (cur-year) 12 31))

(defn end-of-year-str
  "Last date of the current year in a string format."
  []
  (date->str (end-of-year)))

(defn days-left-year
  "Days left until the end of the current year or the specified date."
  ([]
   (days-left-year (end-of-year)))
  ([end-date]
   (time/time-between (date-now) end-date :days)))

(defn weeks-left-year
  "Weeks left until the end of the current year or the specified date."
  ([]
   (weeks-left-year (end-of-year)))
  ([end-date]
   (/ (days-left-year end-date) 7.0)))

(comment
  (date-now)
  (date->str (date-now))
  (str->date "2020-08-05")
  (date-now-string)
  (cur-year)
  (end-of-year)

  ; var for testing a custom end date: create a date string
  (def end-date-test (date->str (time/plus (time/local-date) (time/days 21))))
  (println "Test end-date is:" end-date-test)

  (days-left-year)
  (days-left-year (str->date end-date-test))
  (weeks-left-year)
  (weeks-left-year (str->date end-date-test)))