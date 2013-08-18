(ns com.puppetlabs.service-sandbox.core
  (:use [com.puppetlabs.service-sandbox.test-ring-app :only [run-foo]]))

(run-foo)
