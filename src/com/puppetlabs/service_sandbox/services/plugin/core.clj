(ns com.puppetlabs.service-sandbox.services.plugin.core
  (:use [com.puppetlabs.utils :only [pprint-to-string]]))

(defn initialize
  [log config app-graph]
  (log :info "Initializing plugin service")

  ;; still need to determine how to make the plugin service run *last*,
  ;; and how to make sure that it doesn't cause the already-initialized
  ;; services to re-initialize.  Maybe recompiling the graph is a bad
  ;; idea; maybe the plugin management should not be a service, and
  ;; should simply be a function that gets applied to the main
  ;; app graph before it gets compiled?
  ;;
  ;; That would be functionally sufficient if we only need to support
  ;; plugins at boot, and not support hot-swap.  Would have the
  ;; additional advantage of not requiring a recompile of the
  ;; graph--and it doesn't necessarily seem like prismatic
  ;; intended for the graph to be recompiled.
  ;;
  ;; Downside is that then the plugin management could not have
  ;; access to the logging or config services.
  ;;
  ;; Another option to ponder is the idea that perhaps the
  ;; graph compilation should not have any side effects; but
  ;; then we'd need a way to walk the graph and call initializers
  ;; on all of the services (in the correct order) after
  ;; the compilation.  Not sure whether that would solve it;
  ;; need to think about it more.
  (log :info (str "  Should add plugins to app-graph ("
               (type app-graph)
               "):\n"
               (pprint-to-string app-graph))))