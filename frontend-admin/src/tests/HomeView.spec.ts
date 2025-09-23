import { mount } from '@vue/test-utils'
import HomeView from '../views/HomeView.vue'
import { describe, it, expect } from 'vitest'

describe('HomeView', () => {
  it('renders page', () => {
    const w = mount(HomeView)
    expect(w.html().length).toBeGreaterThan(0)
  })
})
