package io.quarkus.arc;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;

import jakarta.enterprise.context.ContextNotActiveException;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.util.TypeLiteral;

/**
 * Represents a container instance.
 *
 * @author Martin Kouba
 */
public interface ArcContainer {

    /**
     * Unlike {@link BeanManager#getContext(Class)} this method does not throw
     * {@link jakarta.enterprise.context.ContextNotActiveException} if there is no active context for the given
     * scope.
     *
     * @param scopeType
     * @return the active context or null
     * @throws IllegalArgumentException if there is more than one active context for the given scope
     */
    InjectableContext getActiveContext(Class<? extends Annotation> scopeType);

    /**
     *
     * @param scopeType
     * @return immutable list of the matching context objects, never null
     */
    List<InjectableContext> getContexts(Class<? extends Annotation> scopeType);

    /**
     *
     * @return the set of all supported scopes
     */
    Set<Class<? extends Annotation>> getScopes();

    /**
     * Never returns null. However, the handle is empty if no bean matches/multiple beans match the specified type and
     * qualifiers.
     *
     * @param type
     * @param qualifiers
     * @return a new instance handle
     * @throws IllegalArgumentException if an instance of an annotation that is not a qualifier type is given
     */
    <T> InstanceHandle<T> instance(Class<T> type, Annotation... qualifiers);

    /**
     * Never returns null. However, the handle is empty if no bean matches/multiple beans match the specified type and
     * qualifiers.
     *
     * @param type
     * @param qualifiers
     * @return a new instance handle
     * @throws IllegalArgumentException if an instance of an annotation that is not a qualifier type is given
     */
    <T> InstanceHandle<T> instance(TypeLiteral<T> type, Annotation... qualifiers);

    /**
     * Never returns null. However, the handle is empty if no bean matches/multiple beans match the specified type and
     * qualifiers.
     *
     * @param type
     * @param qualifiers
     * @return a new instance handle
     * @throws IllegalArgumentException if an instance of an annotation that is not a qualifier type is given
     */
    <X> InstanceHandle<X> instance(Type type, Annotation... qualifiers);

    /**
     * Never returns null. However, the handle is empty if no bean matches/multiple beans match the specified name.
     *
     * @param name
     * @return a new instance handle
     * @see InjectableBean#getName()
     */
    <T> InstanceHandle<T> instance(String name);

    /**
     * Returns a supplier that can be used to create new instances, or null if no matching bean can be found.
     *
     * @param type
     * @param qualifiers
     * @param <T>
     * @return
     */
    <T> Supplier<InstanceHandle<T>> beanInstanceSupplier(Class<T> type, Annotation... qualifiers);

    /**
     *
     * @param bean
     * @return a new bean instance handle
     */
    <T> InstanceHandle<T> instance(InjectableBean<T> bean);

    /**
     * Instances of dependent scoped beans obtained with the returned injectable instance must be explicitly destroyed, either
     * via the {@link Instance#destroy(Object)} method invoked upon the same injectable instance or with
     * {@link InstanceHandle#destroy()}.
     *
     * If no qualifier is passed, the <tt>@Default</tt> qualifier is assumed.
     *
     * @param <T>
     * @param type
     * @param qualifiers
     * @return a new injectable instance that could be used for programmatic lookup
     */
    <T> InjectableInstance<T> select(Class<T> type, Annotation... qualifiers);

    /**
     * Instances of dependent scoped beans obtained with the returned injectable instance must be explicitly destroyed, either
     * via the {@link Instance#destroy(Object)} method invoked upon the same injectable instance or with
     * {@link InstanceHandle#destroy()}.
     *
     * If no qualifier is passed, the <tt>@Default</tt> qualifier is assumed.
     *
     * @param <T>
     * @param type
     * @param qualifiers
     * @return a new injectable instance that could be used for programmatic lookup
     */
    <T> InjectableInstance<T> select(TypeLiteral<T> type, Annotation... qualifiers);

    /**
     * List all beans matching the required type and qualifiers.
     * <p>
     * Instances of dependent scoped beans should be explicitly destroyed with {@link InstanceHandle#destroy()}.
     * <p>
     * The list is sorted by {@link InjectableBean#getPriority()}. Higher priority goes first.
     *
     * @param <T>
     * @param type
     * @param qualifiers
     * @return the list of handles for the disambiguated beans
     * @see All
     */
    <T> List<InstanceHandle<T>> listAll(Class<T> type, Annotation... qualifiers);

    /**
     * List all beans matching the required type and qualifiers.
     * <p>
     * Instances of dependent scoped beans should be explicitly destroyed with {@link InstanceHandle#destroy()}.
     * <p>
     * The list of is sorted by {@link InjectableBean#getPriority()}. Higher priority goes first.
     *
     * @param <T>
     * @param type
     * @param qualifiers
     * @return the list of handles for the disambiguated beans
     * @see All
     */
    <T> List<InstanceHandle<T>> listAll(TypeLiteral<T> type, Annotation... qualifiers);

    /**
     *
     * @param <X>
     * @param type
     * @param qualifiers
     * @return the list of handles for the disambiguated beans
     * @see #listAll(Class, Annotation...)
     */
    <X> List<InstanceHandle<X>> listAll(Type type, Annotation... qualifiers);

    /**
     * Returns true if Arc container is running.
     * This can be used as a quick check to determine CDI availability in Quarkus.
     *
     * @return true if {@link ArcContainer} is running, false otherwise
     */
    boolean isRunning();

    /**
     *
     * @param beanIdentifier
     * @return an injectable bean or null
     * @see InjectableBean#getIdentifier()
     */
    <T> InjectableBean<T> bean(String beanIdentifier);

    /**
     * Note that ambiguous names are detected at build time. Therefore, unlike
     * {@link jakarta.enterprise.inject.spi.BeanManager.getBeans(String)} this method either returns a resolved bean or
     * {@code null} if no bean matches.
     *
     * @param name
     * @return an injectable bean with the given name or null
     * @see InjectableBean#getName()
     */
    InjectableBean<?> namedBean(String name);

    /**
     * This method never throws {@link ContextNotActiveException}.
     *
     * @return the built-in context for {@link jakarta.enterprise.context.RequestScoped}
     */
    ManagedContext requestContext();

    /**
     * This method never throws {@link ContextNotActiveException}.
     *
     * @return the built-in context for {@link jakarta.enterprise.context.SessionScoped}
     */
    ManagedContext sessionContext();

    /**
     * NOTE: Not all methods are supported!
     *
     * @return the bean manager
     */
    BeanManager beanManager();

    /**
     * @return the default executor service
     */
    ExecutorService getExecutorService();

    /**
     *
     * @return the factory
     * @see CurrentContext
     */
    CurrentContextFactory getCurrentContextFactory();

    /**
     * Indicates whether container runs in strict compatibility mode.
     * Default value is false.
     *
     * @return true is strict mode is enabled, false otherwise.
     */
    boolean strictCompatibility();

    /**
     *
     * @param eventType
     * @param eventQualifiers
     * @return an ordered list of observer methods
     */
    <T> List<InjectableObserverMethod<? super T>> resolveObserverMethods(Type eventType, Annotation... eventQualifiers);
}
