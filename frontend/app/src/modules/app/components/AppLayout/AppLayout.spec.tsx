import React from 'react';
import { render } from 'test/utils';
import userEvent from '@testing-library/user-event';
import { sandbox } from '@insight/testing';

import { Base } from './AppLayout.stories';

const { innerWidth: initialWidth } = window;

describe('<AppLayout />', () => {
  describe('Desktop', () => {
    beforeEach(() => {
      Object.defineProperty(window, 'innerWidth', {
        writable: true,
        configurable: true,
        value: 1024,
      });
    });

    afterEach(() => {
      Object.defineProperty(window, 'innerWidth', {
        writable: true,
        configurable: true,
        value: initialWidth,
      });
    });

    it('Should correctly toggle sidebar', async () => {
      const { getByText, findByText, queryByText, container, push } = render(
        <Base />
      );

      userEvent.hover(container.querySelector('a[href="/"]') as HTMLElement);
      await findByText('Insights');

      const toggleSidebarIcon = container.querySelector(
        'svg[id="sidebar--togle"]'
      ) as HTMLElement;
      userEvent.hover(toggleSidebarIcon);
      await findByText('Collapse');

      userEvent.click(toggleSidebarIcon);
      const sessionsItem = await findByText('Sessions');

      userEvent.click(sessionsItem);
      sandbox.assert.calledWithExactly(push, '/sessions', '/sessions', {
        shallow: undefined,
      });

      // Sidebar collapses on outside click
      userEvent.click(getByText('Some content'));
      expect(queryByText('Sessions')).toBeNull();
    });
  });

  describe('Mobile', () => {
    beforeEach(() => {
      Object.defineProperty(window, 'innerWidth', {
        writable: true,
        configurable: true,
        value: 500,
      });
    });

    afterEach(() => {
      Object.defineProperty(window, 'innerWidth', {
        writable: true,
        configurable: true,
        value: initialWidth,
      });
    });

    it('Should correctly toggle sidebar', async () => {
      const { getByText, queryByText, findByText, container, push } = render(
        <Base />
      );

      const toggleSidebarIcon = container.querySelector(
        'svg[title="Menu"]'
      ) as HTMLElement;

      userEvent.hover(toggleSidebarIcon);
      await findByText('Open sidebar');

      userEvent.click(toggleSidebarIcon);
      const sessionsItem = await findByText('Sessions');

      userEvent.click(sessionsItem);
      sandbox.assert.calledWithExactly(push, '/sessions', '/sessions', {
        shallow: undefined,
      });

      // Sidebar collapses on outside click
      userEvent.click(getByText('Some content'));
      expect(queryByText('Sessions')).toBeNull();

      userEvent.click(toggleSidebarIcon);
      await findByText('Sessions');

      // Sidebar collapses on menu click
      userEvent.click(toggleSidebarIcon);
      expect(queryByText('Sessions')).toBeNull();
    });
  });
});
